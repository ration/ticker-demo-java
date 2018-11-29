import {Component} from '@angular/core';
import {News} from './news.model';
import {TickerService} from './ticker-service';
import {Subscription} from 'rxjs';
import {ChangeContext, Options} from 'ng5-slider';


@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  expanded = false;
  subscription: Subscription = null;
  news: News = {id: 3, text: 'this is some longer text', description: 'News item', breaking: false};
  speed = 100;
  options: Options = {
    floor: 0,
    ceil: 200
  };
  stop_button = "stop";

  constructor(private tickerService: TickerService) {
  }


  /**
   * Pauses the websocket that it no longer requests more items
   */
  public stop() {
    this.tickerService.pause();
    this.expanded = !this.expanded;
    if (this.expanded) {
      this.stop_button = "resume";
    } else {
      this.stop_button = "stop";
    }
  }


  public subscribe() {
    this.subscription = this.tickerService.ticker().subscribe(data => {
      this.news = data;
    });
  }

  public connect() {
    if (this.subscription == null) {
      this.tickerService.initSocket();
      this.subscribe();
    }
  }


  public getPriority() {
    return this.news.breaking ? 'alert-danger' : 'alert-primary';
  }

  setSpeed() {
      this.tickerService.setSpeed(this.speed);
  }
}
