package su.plo.voice.api.addon.annotation.processor;

import com.google.gson.Gson;
import su.plo.voice.api.addon.AddonContainer;
import su.plo.voice.api.addon.annotation.Addon;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SupportedAnnotationTypes({"su.plo.voice.api.addon.annotation.Addon"})
public class AddonAnnotationProcessor extends AbstractProcessor {

    private final Gson gson = new Gson();
    private ProcessingEnvironment environment;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        this.environment = processingEnv;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) return false;

        List<JsonAddon> addons = new ArrayList<>();

        for (Element element : roundEnv.getElementsAnnotatedWith(Addon.class)) {
            if (element.getKind() != ElementKind.CLASS) {
                environment.getMessager().printMessage(
                        Diagnostic.Kind.ERROR,
                        "Only classes can be annotated with " + Addon.class.getCanonicalName()
                );
                return false;
            }

            Name qualifiedName = ((TypeElement) element).getQualifiedName();

            Addon addon = element.getAnnotation(Addon.class);
            if (!AddonContainer.ID_PATTERN.matcher(addon.id()).matches()) {
                environment.getMessager().printMessage(
                        Diagnostic.Kind.ERROR,
                        "Invalid ID for addon " + qualifiedName
                );
                return false;
            }

            addons.add(JsonAddon.from(addon, qualifiedName.toString()));
        }

        if (addons.size() > 0) {
            try {
                FileObject object = environment.getFiler()
                        .createResource(StandardLocation.CLASS_OUTPUT, "", "plasmovoice-addons.json");
                try (Writer writer = new BufferedWriter(object.openWriter())) {
                    gson.toJson(addons, writer);
                }
            } catch (IOException e) {
                environment.getMessager().printMessage(Diagnostic.Kind.ERROR, "Unable to generate addons file");
            }
        }

        return false;
    }
}
