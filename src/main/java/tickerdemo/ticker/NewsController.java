package tickerdemo.ticker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class NewsController {

    private final NewsProvider newsProvider;

    public NewsController(@Autowired NewsProvider newsProvider) {
        this.newsProvider = newsProvider;
    }

    @PostMapping("/speed/{speed}")
    public void speed(@PathVariable Long speed) {
        newsProvider.setSpeed(speed);
    }
}
