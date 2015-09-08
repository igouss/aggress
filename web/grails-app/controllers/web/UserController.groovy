package web

import com.naxsoft.Password
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm

class UserController {
    private static def key = "NjhhNjE2MjUyYTM5YjY2ZDVjY2E5NWFlN2JkNGUzZDM2YTk0MzFjMg"

    def index() {}

    def logout() {
        session.user = null;
        redirect(uri: '/')
    }

    def login() {
        // https://github.com/jwtk/jjwt
        if (params.username != null && params.password != null) {
            def user = User.find("from User as u where u.username = :username", [username: params.username])
            if (null != user) {

                if (Password.check(params.password, user.password)) {
                    session.user = Jwts.builder().setSubject(params.username)
                            .signWith(SignatureAlgorithm.HS256, key)
                            .compact();
                    flash.message = "Login succeed"
                    redirect(uri: '/')
                } else {
                    flash.message = "Invalid password"
                    redirect(controller: "user", action: "index");
                }
            } else {
                flash.message = "Login failed"
                redirect(controller: "user", action: "index");
            }
        } else {
            flash.message = "Login failed"
            redirect(controller: "user", action: "index");
        }
    }

    def register() {}

    def saveUser() {
        if (params.username != null && params.password != null && params.password2 != null) {
            if(null == User.find("from User as u where u.username = :username", [username: params.username])) {
                if (params.password.equals(params.password2)) {
                    def hash = Password.getSaltedHash(params.password)
                    def user = new User(username: params.username, password: hash)
                    user.save()
                } else {
                    flash.message = "Passwords are not the same"
                }
                redirect(controller: "frontPage", action: "index");
            } else {
                flash.message = "User with this email already exists"
                redirect(controller: "user", action: "register");
            }
        } else {
            flash.message = "Required fields are missing"
            redirect(controller: "user", action: "register");
        }
    }
}
