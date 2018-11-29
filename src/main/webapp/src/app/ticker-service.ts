import {Injectable} from '@angular/core';
import RSocketWebSocketClient from 'rsocket-websocket-client';
import {JsonSerializers, RSocketClient, MAX_STREAM_ID} from 'rsocket-core';
import {News} from './news.model';
import {Observable, Subject, Subscription} from 'rxjs';
import {HttpClient} from '@angular/common/http';


/**
 * Provides an observable for the News feed through a RSocket WebSocket.
 * Also can set the speed for the back end of the news generator.
 */
@Injectable({
  providedIn: 'root'
})
export class TickerService {
  private url = "http://localhost:8080";

  private host = 'localhost';
  private port = 9988;
  private subject = new Subject<News>();
  private previous: News = null;
  private subscription = null;

  // With RSocket we define how many items we want from the producer. Similarly as RX operators, this can even be unbounded
  // (set to MAX_STREAM_ID from roscket-core). In this case the websocket continually receives
  // data from the backend (if available). If the data flow is intense and the client has hiccups
  // it ca freeze the client and the websocketconnection dies on the server side (DirectMemoryError).
  // We'll set it to a mere 1, and continually request the next one. Performance wise this should be higher.
  readonly SINGLE_REQ = 1;

  private counter = this.SINGLE_REQ;
  private paused = false;

  constructor(private http: HttpClient) {
  }

  public ticker(): Observable<News> {
    return this.subject.asObservable();
  }

  public initSocket() {
    const client = new RSocketClient({
      // send/receive objects instead of strings/buffers
      serializers: JsonSerializers,
      setup: {
        // ms btw sending keepalive to server
        keepAlive: 60000,
        // ms timeout if no keepalive response
        lifetime: 180000,
        // format of `data`
        dataMimeType: 'application/json',
        // format of `metadata`
        metadataMimeType: 'application/json',
      },
      transport: new RSocketWebSocketClient({
        url: 'ws://' + this.host + ':' + this.port,
        wsCreator: url => {
          return new WebSocket(url);
        }
      })
    });
    const self = this;
    // Connect to the back end RSocket and request a stream (connects to the handler() method in NewsSocket.kt)
    client.connect().subscribe({
      onComplete: socket => {
        // The data and metadata parameters could be used by the handler() payload parameter on the back end side
        socket.requestStream({data: '', metadata: ''}).subscribe({
          onComplete() {
            console.log('onComplete()');
          },
          onError(error) {
            console.log('onError(%s)', error.message);
          },
          onNext(payload) {
            self.handlePayload(payload);
            self.requestMoreDataIfNeeded();
          },
          onSubscribe(_subscription) {
            self.subscription = _subscription;
            _subscription.request(self.SINGLE_REQ);
          }
        });
      },
      onError: error => console.error(error),
      onSubscribe: cancel => {/* call cancel() to abort */
      },
      onNext: data => console.log(data)
    });


  }

  public pause() {
    this.paused = !this.paused;
    this.requestMoreDataIfNeeded();
  }


  private handlePayload(payload) {
    this.subject.next(payload.data);
    // Here we can observe backpressure. We assume that the news feed is always in id order, and something was
    // dropped if the sequence is not in order
    if (this.previous != null && (this.previous.id + 1) !== payload.data.id) {
      console.log('Missed ' + payload.data.id + ' previous ' + this.previous.id);
    }
    this.previous = payload.data;
  }

  private requestMoreDataIfNeeded() {
    if (!this.paused) {
      this.counter--;
      if (this.subscription != null && this.counter <= 0) {
        this.subscription.request(this.SINGLE_REQ);
        this.counter = this.SINGLE_REQ;
      }
    }
  }

  setSpeed(speed: number) {
    this.http.post(this.url + '/speed/' + speed, null).subscribe(() => console.log('Saved speed'));
  }
}
