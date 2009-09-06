import org.springframework.beans.factory.InitializingBean  
import grails.util.JSonBuilder  
  
class StockService implements InitializingBean {  
    // we don't do anything transactional  
    boolean transactional = false  
  
    // let it be auto-wired from Cometd Plugin  
    def bayeux  
  
    // our publisher  
    def client  
  
    def quotes = []  
  
    // just like @PostConstruct  
    void afterPropertiesSet() {  
        client = bayeux.newClient(this.class.name)  
  
        quotes << bayeux.getChannel('/quotes/AAPL', true)  
        quotes << bayeux.getChannel('/quotes/GOOG', true)  
        quotes << bayeux.getChannel('/quotes/YHOO', true)  
  
        Thread.startDaemon {  
            def rnd = new Random()  
  
            while (true) {  
                quotes.each {  
                    def writer = new StringWriter();  
                    new JSonBuilder(writer).json {  
                        // I'm lazy, just generate random numbers  
                        lastTrade ((float)rnd.nextInt(100) / 10f)  
                        change (0.5f - (float)rnd.nextInt(100) / 100f)  
                        volume rnd.nextInt(500)  
                    }  
                    // on behalf of client, publish JSON result to selected channel  
                    it.publish(client, writer.toString(), null)  
                }  
                try {  
                    // delay  
                    Thread.sleep(3000)  
                } catch (InterruptedException ex) {  
                    // do nothing  
                }  
            }  
        }  
  
    }  
}