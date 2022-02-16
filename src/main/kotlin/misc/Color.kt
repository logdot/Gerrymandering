package misc

class Color(r: Int, g: Int, b: Int) {
    var r = 0
        set(value) {
            gray = (r + g + b)/3
            field = value
        }

    var g = 0
        set(value) {
            gray = (r + g + b)/3
            field = value
        }

    var b = 0
        set(value) {
            gray = (r + g + b)/3
            field = value
        }

    var gray: Int = (r + g + b)/3
        private set

    init {
        this.r = r
        this.g = g
        this.b = b
    }
}