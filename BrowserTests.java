import org.junit.Test;

import java.io.IOException;

public class BrowserTests {

    @Test
    public void htmlPageTest() throws IOException {
        String domain = "slacksite.com";
        String page = "other/ftp.html";

        Browser.httpGET(domain, page);
    }

    @Test
    public void imageTest() throws IOException {
        String domain = "http://craphound.com";
        String image = "images/1006884_2adf8fc7.jpg";

        Browser.httpGET(domain, image);
    }
}
