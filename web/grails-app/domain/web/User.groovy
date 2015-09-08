package web

class User {

    static constraints = {

    }
    static mapping = {
        table name: "accounts", schema: "guns"
        username unique: true
    }

    String username
    String password

    User() {
    }

}
