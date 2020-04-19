package me.ibrahimsn.particle

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.random.Random

class ParticleView : SurfaceView, SurfaceHolder.Callback {

    private val particles = mutableListOf<Particle>()
    private var surfaceViewThread: SurfaceViewThread? = null

    private var count = 20
    private var minRadius = 5
    private var maxRadius = 10
    private var isLinesEnabled = true
    private var hasSurface: Boolean = false

    private var background = Color.BLACK
    private var colorParticles = Color.WHITE
    private var colorLines = Color.WHITE
    private val path = Path()

    private val paintParticles: Paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        strokeWidth = 2F
    }

    private val paintLines: Paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = 2F
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.ParticleView, 0, 0)

        isLinesEnabled = a.getBoolean(R.styleable.ParticleView_lines, isLinesEnabled)
        count = a.getInt(R.styleable.ParticleView_particleCount, count)
        minRadius = a.getInt(R.styleable.ParticleView_minParticleRadius, minRadius)
        maxRadius = a.getInt(R.styleable.ParticleView_maxParticleRadius, maxRadius)
        colorParticles = a.getColor(R.styleable.ParticleView_particleColor, colorParticles)
        colorLines = a.getColor(R.styleable.ParticleView_linesColor, colorLines)
        background = a.getColor(R.styleable.ParticleView_backgroundColor, background)
        a.recycle()

        paintParticles.color = colorParticles
        paintLines.color = colorLines

        if (count > 50) count = 50
        if (minRadius <= 0) minRadius = 1
        if (maxRadius <= minRadius) maxRadius = minRadius + 1

        if (holder != null) {
            holder.addCallback(this)
        }

        hasSurface = false
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

        if (surfaceViewThread != null) {
            surfaceViewThread!!.requestExitAndWait()
            surfaceViewThread = null
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {
        // Ignore
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (particles.size == 0) {
            for (i in 0 until count) {
                particles.add(
                    Particle(
                        Random.nextInt(minRadius, maxRadius).toFloat(),
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
            while (running) {
                try {
                    canvas = holder.lockCanvas()

                    synchronized (holder) {
                        canvas?.drawColor(background)

                        for (i in 0 until count) {
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
                                if (isLinesEnabled) {
                                    for (j in 0 until count) {
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
            } catch (ignored: InterruptedException) {

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