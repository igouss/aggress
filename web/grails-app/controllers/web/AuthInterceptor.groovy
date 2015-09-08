package web


class AuthInterceptor {
    AuthInterceptor() {
//        def match = matchAll()
//        match.excludes(controller: "user", action: "login")
//        match.excludes(controller: "user", action: "index")
//        match.excludes(controller: "user", action: "logout")
//        match.excludes(controller: "frontPage", action: "index")
    }

    boolean before() {
        return false;
//        if (!session.user) {
//            redirect(controller: "user", action: 'login')
//            return false
//        } else {
//            return true;
//        }
    }

    boolean after() { false }

    void afterView() {
        // no-op
    }

}
