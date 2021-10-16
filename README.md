## Добавление Plasmo Voice в проект

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
            <artifactId>server</artifactId>
            <version>1.0.7</version>
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
    compileOnly 'su.plo.voice:server:1.0.7'
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
    compileOnly("su.plo.voice:server:1.0.7")
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

Мне лень их дублировать, все методы есть
в [PlasmoVoiceAPI](https://github.com/plasmoapp/plasmo-voice/tree/main-spigot/src/main/java/su/plo/voice/PlasmoVoiceAPI.java)