# Plasmo Voice Spigot API

## Adding to the project

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
            <version>1.0.11</version>
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
    compileOnly 'su.plo.voice:spigot:1.0.11'
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
    compileOnly("su.plo.voice:spigot:1.0.11")
}
```

## Obtaining an instance of the API

### Using the Bukkit ServicesManager

```java
RegisteredServiceProvider<PlasmoVoiceAPI> provider = Bukkit.getServicesManager().getRegistration(PlasmoVoiceAPI.class);
if(provider != null) {
    PlasmoVoiceAPI api = provider.getProvider();
}
```

### Using the singleton (static access)

```java
PlasmoVoiceAPI api = PlasmoVoice.getInstance();
```

## Methods and Events

For methods see this [class](https://github.com/plasmoapp/plasmo-voice/tree/main-spigot/src/main/java/su/plo/voice/PlasmoVoiceAPI.java)

For events see this [package](https://github.com/plasmoapp/plasmo-voice/tree/main-spigot/src/main/java/su/plo/voice/events)

## Examples of API usage

Example #1: Full class with simple remote voice chat, linking players together by holding an iron ingot.

```java
public final class APIExample extends JavaPlugin implements Listener {
    private static final PlasmoVoiceAPI api = PlasmoVoice.getInstance();

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
    }

    @EventHandler
    public void onSpeak(PlayerSpeakEvent event) {
        if (!event.getPlayer().getEquipment().getItemInMainHand().getType().equals(Material.IRON_INGOT)) return;
        event.setCancelled(true);
        for (Player player : api.getConnectedPlayers()) {
            if (!player.getEquipment().getItemInMainHand().getType().equals(Material.IRON_INGOT)) continue;
            api.sendVoicePacketToPlayer(event.getPacket(), player);
        }
    }
}
```

Example #2: Send "Hey, you're talking!" to player which is started talking.

```java
@EventHandler
public void onStartSpeak(PlayerStartSpeakEvent event) {
    event.getPlayer().sendMessage("Hey, you're talking!");
}
```