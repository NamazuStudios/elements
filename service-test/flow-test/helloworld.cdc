pub contract HelloWorld {

    pub let greeting: String

    pub fun hello(): String {
        return self.greeting
    }

    init() {
        self.greeting = "Hello World: "
    }

}

