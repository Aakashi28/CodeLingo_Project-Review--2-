import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import javax.servlet.ServletException;
import javax.servlet.http.*;

public class LoginServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            Connection con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/language_platform",
                "root",
                "password"
            );

            PreparedStatement ps = con.prepareStatement(
                "SELECT role FROM users WHERE email=? AND password=?"
            );

            ps.setString(1, email);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                out.println("<h3>Login Successful</h3>");
                out.println("<p>Role: " + rs.getString("role") + "</p>");
            } else {
                out.println("<h3>Invalid Login</h3>");
            }

            con.close();

        } catch (Exception e) {
            out.println("<h3>Error Occurred</h3>");
        }
    }
}
