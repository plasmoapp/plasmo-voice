## Adding Plasmo Voice to the project

### Maven

```xml
<project>
    <repositories>
        <repository>
            <id>plasmo-repo</id>
            <url>https://repo.plo.su</url>
        </repository>
    </repositories>
    
    <dependencies>
        <dependency>
            <groupId>su.plo.voice</groupId>
            <artifactId>spigot</artifactId>
            <version>1.0.9-alpha</version>
            <scope>provided</scope>
        </dependency>
    </dependencies>
</project>
```

### Groovy DSL

```groovy
repositories {
    maven { url 'https://repo.plo.su/' }
}

dependencies {
    compileOnly 'su.plo.voice:spigot:1.0.9-alpha'
}
```

### Kotlin DSL

```
repositories {
    maven {
        url = uri("https://repo.plo.su")
    }
}

dependencies {
    compileOnly("su.plo.voice:spigot:1.0.9-alpha")
}
```

## Obtaining an instance of the API

### Using the Bukkit ServicesManager

```java
RegisteredServiceProvider<PlasmoVoiceAPI> provider=Bukkit.getServicesManager().getRegistration(PlasmoVoiceAPI.class);
        if(provider!=null){
        PlasmoVoiceAPI api=provider.getProvider();
        }
```

### Using the singleton (static access)

```java
PlasmoVoiceAPI api=PlasmoVoice.getInstance();
```

## Methods

See
this: [PlasmoVoiceAPI](https://github.com/plasmoapp/plasmo-voice/blob/spigot-distances/src/main/java/su/plo/voice/PlasmoVoiceAPI.java)