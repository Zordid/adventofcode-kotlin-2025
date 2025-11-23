package utils

import utils.KPixelGameEngine.Companion.gradientColor
import java.awt.Color
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlin.reflect.KProperty
import kotlin.time.Duration

fun KPixelGameEngine.animation(duration: Duration, fps: Int = limitFps, code: AnimationContext.() -> Unit): Animation =
    SingleAnimation(duration, fps, code)

fun KPixelGameEngine.animation(frames: Int, code: AnimationContext.() -> Unit): Animation =
    SingleAnimation(frames.toLong(), code)

fun KPixelGameEngine.animation(frames: Long, code: AnimationContext.() -> Unit): Animation =
    SingleAnimation(frames, code)

fun action(code: () -> Unit): Animation = Action(code)

interface Animation {
    val frames: Long
    val ended: Boolean
    fun update()
}

val Animation?.active get() = this?.ended == false

infix fun Animation.then(next: Animation) = CombinedAnimation(this, next)

class Action(val code: () -> Unit) : Animation {
    private var hasRun = false
    override val frames: Long
        get() = 1
    override val ended: Boolean
        get() = hasRun

    override fun update() {
        if (!hasRun) code().also { hasRun = true }
    }
}

class CombinedAnimation(private val first: Animation, private val second: Animation) : Animation {
    override val frames: Long
        get() = first.frames + second.frames
    override val ended: Boolean
        get() = second.ended

    override fun update() {
        if (!first.ended) first.update() else second.update()
    }
}

fun parallel(vararg animation: Animation): Animation = ParallelAnimations(animation.asList())

class ParallelAnimations(private val animations: List<Animation>) : Animation {
    override val frames = animations.maxOf { it.frames }

    override val ended
        get() = animations.all { it.ended }

    override fun update() {
        animations.forEach { if (!it.ended) it.update() }
    }
}

class SingleAnimation(override val frames: Long, val code: AnimationContext.() -> Unit) : Animation {

    constructor(duration: Duration, fps: Int, code: AnimationContext.() -> Unit) :
            this((duration.inWholeMilliseconds / 1000.0 * fps).roundToLong().coerceAtLeast(1), code)

    override val ended: Boolean get() = context.currentFrame >= frames
    private val context = AnimationContext(frames)

    override fun update() {
        if (!ended) {
            context.code()
            context.currentFrame++
        }
    }
}

abstract class AnimatedValue<T>(val context: AnimationContext) {
    abstract val value: T
    operator fun getValue(nothing: Nothing?, property: KProperty<*>): T = value
}

class AnimatedPoint(first: Point, last: Point, context: AnimationContext) : AnimatedValue<Point>(context) {
    private val xAnim = AnimatedInt(first.x, last.x, context)
    private val yAnim = AnimatedInt(first.y, last.y, context)
    override val value: Point
        get() = xAnim.value to yAnim.value
}

class AnimatedDouble(private val first: Double, private val last: Double, context: AnimationContext) :
    AnimatedValue<Double>(context) {
    private val size = last - first
    override val value: Double
        get() {
            if (first == last) return first
            val f = context.currentFrame
            val l = context.totalFrames - 1
            val percent = f.toDouble() / l
            return first + (size * percent)
        }
}

class AnimatedInt(private val first: Int, private val last: Int, context: AnimationContext) :
    AnimatedValue<Int>(context) {
    private val size = (last - first).absoluteValue
    private val dir = if (last > first) 1 else -1
    override val value: Int
        get() {
            if (first == last) return first
            val f = context.currentFrame
            val l = context.totalFrames - 1
            return when (f) {
                0L -> first
                l -> last
                else -> (first + dir * size * f.toDouble() / l).roundToInt()
            }
        }
}

class AnimatedColor(private val from: Color, private val to: Color, context: AnimationContext) :
    AnimatedValue<Color>(context) {
    private val percent = AnimatedDouble(0.0, 1.0, context)

    override val value: Color
        get() = gradientColor(from, to, percent.value)
}

class AnimationContext(val totalFrames: Long) {
    var currentFrame = 0L
    val lastFrame = totalFrames - 1
    fun animate(from: Int, to: Int) = AnimatedInt(from, to, this)
    fun animate(from: Double, to: Double) = AnimatedDouble(from, to, this)
    fun animate(from: Point, to: Point) = AnimatedPoint(from, to, this)
    fun animate(from: Color, to: Color) = AnimatedColor(from, to, this)

    fun onStart(code: () -> Unit) {
        if (currentFrame == 0L) code()
    }

    fun onLastFrame(code: () -> Unit) {
        if (currentFrame == totalFrames - 1) code()
    }
}