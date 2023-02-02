
// import 0xf8d6e0586b0a20c7

pub fun main(first: String, age: Int): String {
    return HelloWorld.hello()
        .concat(first)
        .concat(" you are ").concat(age.toString()).concat(" years old.")
}
