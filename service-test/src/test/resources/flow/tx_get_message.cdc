
transaction() {

  prepare(acct: AuthAccount) {
    let hello = acct.borrow<&HelloWorld.HelloAsset>(from: /storage/Hello)
    if hello == nil {
      panic("Couldn't find the asset")
    }
    log(hello?.getMessage())
  }

}