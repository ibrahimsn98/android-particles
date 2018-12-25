# Android Particles
Particle animation library for Android

[![](https://jitpack.io/v/ibrahimsn98/android-particles.svg)](https://jitpack.io/#ibrahimsn98/android-particles)

# Example
<img width="300" src="https://github.com/ibrahimsn98/android-particles/blob/master/art/particle.gif"/>



# Setup
```gradle
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
dependencies {
      implementation 'com.github.ibrahimsn98:android-particles:1.4'
}
```

# Attributions
```xml
<me.ibrahimsn.particle.ParticleView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:particleCount="20"
        app:minParticleRadius="5"
        app:maxParticleRadius="12"
        app:particleColor="@color/colorAccent" />
```

# Inspired From
Thanks to [VincentGarreau](https://github.com/VincentGarreau) for sharing that awesome [javascript library](https://github.com/VincentGarreau/particles.js)

# TODO
- [x] Performance optimizations
- [x] RAM optimizations
