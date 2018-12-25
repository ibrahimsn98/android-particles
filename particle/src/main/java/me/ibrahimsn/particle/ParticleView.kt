package me.ibrahimsn.particle

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.ViewTreeObserver
import kotlin.random.Random

class ParticleView : SurfaceView, SurfaceHolder.Callback {

    private lateinit var particles: Array<Particle?>

    private var surfaceViewThread: SurfaceViewThread? = null

    private var count = 20
    private var minRadius = 5
    private var maxRadius = 10
    private var hasSurface: Boolean = false

    private var background = Color.BLACK
    private var color = Color.WHITE
    private val path = Path()

    private val paint: Paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL_AND_STROKE
        strokeWidth = 2F
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.ParticleView, 0, 0)

        count = a.getInt(R.styleable.ParticleView_particleCount, count)
        minRadius = a.getInt(R.styleable.ParticleView_minParticleRadius, minRadius)
        maxRadius = a.getInt(R.styleable.ParticleView_maxParticleRadius, maxRadius)
        color = a.getColor(R.styleable.ParticleView_particleColor, color)
        background = a.getColor(R.styleable.ParticleView_backgroundColor, background)
        a.recycle()

        particles = arrayOfNulls(count)
        paint.color = color

        if (count > 50) count = 50
        if (minRadius <= 0) minRadius = 1
        if (maxRadius <= minRadius) maxRadius = minRadius + 1

        if (holder != null)
            holder.addCallback(this)

        hasSurface = false
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        hasSurface = true

        if (surfaceViewThread == null)
            surfaceViewThread = SurfaceViewThread()

        surfaceViewThread!!.start()
    }

    fun resume() {
        if (surfaceViewThread == null) {
            surfaceViewThread = SurfaceViewThread()

            if (hasSurface)
                surfaceViewThread!!.start()
        }
    }

    fun pause() {
        if (surfaceViewThread != null) {
            surfaceViewThread!!.requestExitAndWait()
            surfaceViewThread = null
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        hasSurface = false

        if (surfaceViewThread != null) {
            surfaceViewThread!!.requestExitAndWait()
            surfaceViewThread = null
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {

    }

    private inner class SurfaceViewThread : Thread() {

        private var running: Boolean = true

        init {
            running = true

            viewTreeObserver.addOnPreDrawListener(object: ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    if (viewTreeObserver.isAlive)
                        viewTreeObserver.removeOnPreDrawListener(this)

                    for (i in 0 until count)
                        particles[i] = Particle(
                            Random.nextInt(minRadius, maxRadius).toFloat(),
                            Random.nextInt(0, width).toFloat(),
                            Random.nextInt(0, height).toFloat(),
                            Random.nextInt(-2, 2),
                            Random.nextInt(-2, 2),
                            Random.nextInt(150, 255))

                    return true
                }
            })
        }

        override fun run() {
            while (running) {
                var canvas: Canvas? = null

                try {
                    canvas = holder.lockCanvas()

                    synchronized (holder) {
                        canvas.drawColor(background)

                        for (i in 0 until count) {
                            particles[i]!!.x += particles[i]!!.vx
                            particles[i]!!.y += particles[i]!!.vy

                            if (particles[i]!!.x < 0)
                                particles[i]!!.x = width.toFloat()
                            else if (particles[i]!!.x > width)
                                particles[i]!!.x = 0F

                            if (particles[i]!!.y < 0)
                                particles[i]!!.y = height.toFloat()
                            else if (particles[i]!!.y > height)
                                particles[i]!!.y = 0F

                            for (j in 0 until count)
                                linkParticles(canvas, particles[i]!!, particles[j]!!)

                            paint.alpha = particles[i]!!.alpha
                            canvas.drawCircle(particles[i]!!.x, particles[i]!!.y, particles[i]!!.radius, paint)
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

    private fun linkParticles(canvas: Canvas, p1: Particle, p2: Particle) {
        val dx = p1.x - p2.x
        val dy = p1.y - p2.y
        val dist = Math.sqrt((dx * dx + dy * dy).toDouble()).toInt()

        if (dist < 225) {
            path.moveTo(p1.x, p1.y)
            path.lineTo(p2.x, p2.y)

            paint.alpha = 250 - dist
            canvas.drawPath(path, paint)

            path.reset()
        }
    }

    data class Particle constructor(var radius: Float, var x: Float, var y: Float,
                                    var vx: Int, var vy: Int, var alpha: Int)
}