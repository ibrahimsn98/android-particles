# Android Particles
Particle animation library for Android


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
      implementation 'com.github.ibrahimsn98:android-particles:1.2'
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
        app:particleColor="@color/colorAccent"
        app:backgroundColor="@color/colorBackground"/>
```

# Inspired From
Thanks to [VincentGarreau](https://github.com/VincentGarreau) for sharing that awesome [javascript library](https://github.com/VincentGarreau/particles.js)

# TODO
- [ ] Performance optimizations


License
--------
MIT License

Copyright (c) 2018 ibrahim süren

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
