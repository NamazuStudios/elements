
pub contract HelloWorld {
    
    pub var totalCount: UInt64
	  
    pub resource HelloAsset {

      pub var message: String

      pub fun setMessage(msg: String) {
          self.message = msg;
          HelloWorld.totalCount = HelloWorld.totalCount + 1
      }

      pub fun getMessage(): String {
        return self.message
      }

      init() {
          self.message = "Hello"
      }

    }

    pub fun getTotalCount(): UInt64 {
        return self.totalCount
    }

    pub fun hello(): String {
        return "Hello World!"
    }

	init() {
      let newHello <- create HelloAsset()

      self.account.save(<-newHello, to: /storage/Hello)
        
      self.totalCount = 0

      log("HelloAsset created and stored")
	}

}
