package me.ibrahimsn.particle

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import android.view.ViewTreeObserver
import kotlin.random.Random

class ParticleView : View {

    private lateinit var particles: Array<Particle>

    private var count = 20
    private var background = Color.RED
    private var minRadius = 5
    private var maxRadius = 10

    private val paint: Paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL_AND_STROKE
        color = Color.WHITE
        strokeWidth = 2F
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.ParticleView, 0, 0)

        count = a.getInt(R.styleable.ParticleView_particleCount, count)
        minRadius = a.getInt(R.styleable.ParticleView_minParticleRadius, minRadius)
        maxRadius = a.getInt(R.styleable.ParticleView_maxParticleRadius, maxRadius)
        background = a.getColor(R.styleable.ParticleView_backgroundColor, background)

        if (count > 50)
            count = 50

        if (minRadius <= 0)
            minRadius = 1

        if (maxRadius <= minRadius)
            maxRadius = minRadius + 1

        a.recycle()
    }

    init {
        viewTreeObserver.addOnPreDrawListener(object: ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                if (viewTreeObserver.isAlive)
                    viewTreeObserver.removeOnPreDrawListener(this)

                val array = arrayOfNulls<Particle>(count)

                for (i in 0 until count)
                    array[i] = Particle(
                        Random.nextInt(minRadius, maxRadius).toFloat(),
                        Random.nextInt(0, width).toFloat(),
                        Random.nextInt(0, height).toFloat(),
                        Random.nextInt(-2, 2),
                        Random.nextInt(-2, 2),
                        Random.nextInt(150, 255))

                particles =  array as Array<Particle>

                return true
            }
        })
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(background)

        for (i in 0 until count) {
            paint.alpha = particles[i].alpha

            particles[i].x += particles[i].vx
            particles[i].y += particles[i].vy

            if (particles[i].x < 0)
                particles[i].x = width.toFloat()
            else if (particles[i].x > width)
                particles[i].x = 0F

            if (particles[i].y < 0)
                particles[i].y = height.toFloat()
            else if (particles[i].y > height)
                particles[i].y = 0F

            for(j in 0 until count)
                linkParticles(canvas, particles[i], particles[j])

            canvas.drawCircle(particles[i].x, particles[i].y, particles[i].radius, paint)
        }

        postInvalidateDelayed(20)
        invalidate()
    }

    private fun linkParticles(canvas: Canvas, p1: Particle, p2: Particle) {
        val dx = p1.x - p2.x
        val dy = p1.y - p2.y
        val dist = Math.sqrt((dx * dx + dy * dy).toDouble()).toInt()

        if (dist < 200) {
            val path = Path()

            path.moveTo(p1.x, p1.y)
            path.lineTo(p2.x, p2.y)
            path.close()

            paint.alpha = 225 - dist
            canvas.drawPath(path, paint)
        }
    }

    data class Particle constructor(var radius: Float, var x: Float, var y: Float,
                                    var vx: Int, var vy: Int, var alpha: Int)
}