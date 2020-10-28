package me.ibrahimsn.particle

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.random.Random

class ParticleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int = R.attr.ParticleViewStyle
) : SurfaceView(context, attrs, defStyleAttr), SurfaceHolder.Callback {

    private val particles = mutableListOf<Particle>()
    private var surfaceViewThread: SurfaceViewThread? = null
    private var hasSurface: Boolean = false
    private var hasSetup = false

    private val path = Path()

    // Attribute Defaults
    private var _particleCount = 20

    @Dimension
    private var _particleMinRadius = 5

    @Dimension
    private var _particleMaxRadius = 10

    @ColorInt
    private var _particlesBackgroundColor = Color.BLACK

    @ColorInt
    private var _particleColor = Color.WHITE

    @ColorInt
    private var _particleLineColor = Color.WHITE

    private var _particleLinesEnabled = true

    // Core Attributes
    var particleCount: Int
        get() = _particleCount
        set(value) {
            _particleCount = when {
                value > 50 -> 50
                value < 0 -> 0
                else -> value
            }
        }

    var particleMinRadius: Int
        @Dimension get() = _particleMinRadius
        set(@Dimension value) {
            _particleMinRadius = when {
                value <= 0 -> 1
                value >= particleMaxRadius -> 1
                else -> value
            }
        }

    var particleMaxRadius: Int
        @Dimension get() = _particleMaxRadius
        set(@Dimension value) {
            _particleMaxRadius = when {
                value <= particleMinRadius -> particleMinRadius + 1
                else -> value
            }
        }

    var particlesBackgroundColor: Int
        @ColorInt get() = _particlesBackgroundColor
        set(@ColorInt value) {
            _particlesBackgroundColor = value
        }

    var particleColor: Int
        @ColorInt get() = _particleColor
        set(@ColorInt value) {
            _particleColor = value
            paintParticles.color = value
        }

    var particleLineColor: Int
        @ColorInt get() = _particleLineColor
        set(@ColorInt value) {
            _particleLineColor = value
            paintLines.color = value
        }

    var particleLinesEnabled: Boolean
        get() = _particleLinesEnabled
        set(value) {
            _particleLinesEnabled = value
        }

    // Paints
    private val paintParticles: Paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        strokeWidth = 2F
    }

    private val paintLines: Paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL_AND_STROKE
        strokeWidth = 2F
    }

    init {
        obtainStyledAttributes(attrs, defStyleAttr)
        if (holder != null) holder.addCallback(this)
        hasSurface = false
    }

    private fun obtainStyledAttributes(attrs: AttributeSet, defStyleAttr: Int) {
        val typedArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.ParticleView,
            defStyleAttr,
            0
        )

        try {
            particleCount = typedArray.getInt(
                R.styleable.ParticleView_particleCount,
                particleCount
            )

            particleMinRadius = typedArray.getInt(
                R.styleable.ParticleView_particleMinRadius,
                particleMinRadius
            )

            particleMaxRadius = typedArray.getInt(
                R.styleable.ParticleView_particleMaxRadius,
                particleMaxRadius
            )

            particlesBackgroundColor = typedArray.getColor(
                R.styleable.ParticleView_particlesBackgroundColor,
                particlesBackgroundColor
            )

            particleColor = typedArray.getColor(
                R.styleable.ParticleView_particleColor,
                particleColor
            )

            particleLineColor = typedArray.getColor(
                R.styleable.ParticleView_particleLineColor,
                particleLineColor
            )

            particleLinesEnabled = typedArray.getBoolean(
                R.styleable.ParticleView_particleLinesEnabled,
                particleLinesEnabled
            )
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            typedArray.recycle()
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        hasSurface = true

        if (surfaceViewThread == null) {
            surfaceViewThread = SurfaceViewThread()
        }

        surfaceViewThread?.start()
    }

    fun resume() {
        if (surfaceViewThread == null) {
            surfaceViewThread = SurfaceViewThread()

            if (hasSurface) {
                surfaceViewThread?.start()
            }
        }
    }

    fun pause() {
        surfaceViewThread?.requestExitAndWait()
        surfaceViewThread = null
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        hasSurface = false
        surfaceViewThread?.requestExitAndWait()
        surfaceViewThread = null
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {
        // ignored
    }

    private fun setupParticles() {
        if (!hasSetup) {
            hasSetup = true
            particles.clear()
            for (i in 0 until particleCount) {
                particles.add(
                    Particle(
                        Random.nextInt(particleMinRadius, particleMaxRadius).toFloat(),
                        Random.nextInt(0, width).toFloat(),
                        Random.nextInt(0, height).toFloat(),
                        Random.nextInt(-2, 2),
                        Random.nextInt(-2, 2),
                        Random.nextInt(150, 255)
                    )
                )
            }
        }
    }

    private inner class SurfaceViewThread : Thread() {

        private var running = true
        private var canvas: Canvas? = null

        override fun run() {
            setupParticles()

            while (running) {
                try {
                    canvas = holder.lockCanvas()

                    synchronized (holder) {
                        // Clear screen every frame
                        canvas?.drawColor(particlesBackgroundColor, PorterDuff.Mode.SRC)

                        for (i in 0 until particleCount) {
                            particles[i].x += particles[i].vx
                            particles[i].y += particles[i].vy

                            if (particles[i].x < 0) {
                                particles[i].x = width.toFloat()
                            } else if (particles[i].x > width) {
                                particles[i].x = 0F
                            }

                            if (particles[i].y < 0) {
                                particles[i].y = height.toFloat()
                            } else if (particles[i].y > height) {
                                particles[i].y = 0F
                            }

                            canvas?.let {
                                if (particleLinesEnabled) {
                                    for (j in 0 until particleCount) {
                                        if (i != j) {
                                            linkParticles(it, particles[i], particles[j])
                                        }
                                    }
                                }
                            }

                            paintParticles.alpha = particles[i].alpha
                            canvas?.drawCircle(particles[i].x, particles[i].y, particles[i].radius, paintParticles)
                        }
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    if (canvas != null) {
                        holder.unlockCanvasAndPost(canvas)
                    }
                }
            }
        }

        fun requestExitAndWait() {
            running = false

            try {
                join()
            } catch (e: InterruptedException) {
                // ignored
            }
        }
    }

    private var dx: Float = 0f
    private var dy: Float = 0f
    private var dist: Float = 0f
    private var distRatio: Float = 0f

    private fun linkParticles(canvas: Canvas, p1: Particle, p2: Particle) {
        dx = p1.x - p2.x
        dy = p1.y - p2.y
        dist = sqrt(dx * dx + dy * dy)

        if (dist < 220) {
            path.moveTo(p1.x, p1.y)
            path.lineTo(p2.x, p2.y)
            distRatio = (220 - dist) / 220

            paintLines.alpha = (min(p1.alpha, p2.alpha) * distRatio / 2).toInt()
            canvas.drawPath(path, paintLines)

            path.reset()
        }
    }
}