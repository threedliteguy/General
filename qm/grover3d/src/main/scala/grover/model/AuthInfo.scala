package grover.model

case class AuthInfo() {
  //Here you should put the logic for permissions associated to users
  def hasPermissions(permission: String): Boolean = true
}
