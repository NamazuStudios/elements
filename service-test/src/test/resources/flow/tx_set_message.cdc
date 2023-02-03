
transaction(msg: String) {

  prepare(acct: AuthAccount) {
    let hello = acct.borrow<&HelloWorld.HelloAsset>(from: /storage/Hello)
    if hello == nil {
      panic("Couldn't find the asset")
    }
    hello?.setMessage(msg: msg)
    log(hello?.getMessage())
  }

  execute {
    log(HelloWorld.totalCount)
  }
}