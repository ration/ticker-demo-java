Demonstrating Backpressure with RSockets

You need angular cli and gradle 4 to execute the project and then just

> gradle bootRun

Open the browser to http://localhost:8080 and press connect. You can se set the item speed or stop the flow by clicking (expanding)  a news item. The lower you get the higher the likelihood of backpressure. By default the back end will only let breaking news through for 10 seconds.