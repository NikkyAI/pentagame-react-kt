actual val Char.isUpperCase: Boolean
    get() = (this == this.toUpperCase() && this != this.toLowerCase())