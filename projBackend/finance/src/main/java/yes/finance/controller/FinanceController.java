package yes.finance.controller; 

import java.util.*;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.SortDefault;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.util.SystemPropertyUtils;
import org.springframework.data.domain.Sort;

import yes.finance.model.*;
import yes.finance.model.Currency;
import yes.finance.services.*;

@CrossOrigin
@RestController
public class FinanceController {

    @Autowired
    private SimpMessageSendingOperations sendingOperations;

    @Autowired
    private UserService service;
    @Autowired
    private CurrencyService currencyservice;
    @Autowired
    private ExtensionService extensionservice;
    @Autowired
    private MarketService marketservice;
    @Autowired
    private OrderService orderservice;
    @Autowired
    private TransactionService transactionservice;
    @Autowired
    private PortfolioService portfolioservice;
    @Autowired
    private TickerService tickerservice;
    @Autowired
    private UserService userService;

    private Random random = new Random();

    private Portfolio systemPortfolio;

    //// INIT ///

    @PostConstruct
    public void init() {
        systemPortfolio = new Portfolio("system");
        portfolioservice.savePortfolio(systemPortfolio);
    }

    //////////////////////////////////////////// USER
    //////////////////////////////////////////// ////////////////////////////////////////////

    @GetMapping("/user")
    public Page<User> getAllUsers(Pageable pageable) {
        return service.getUsers(pageable);
    }

    @PostMapping("/user")
    public User createUsers(@RequestBody User user) {
        return service.saveUser(user);
    }

    @DeleteMapping("/user/{id}")
    public String deleteUsers(@PathVariable int id) {
        return service.deleteUser(id);
    }

    //////////////////////////////////////////// CURRENCY
    //////////////////////////////////////////// ////////////////////////////////////////////

    // obter info sobre a moeda (nome, symbol e isso)
    @GetMapping("/currency/info/{id}")
    public Currency getCurrencyInfo(@PathVariable int id) {
        return currencyservice.getCurrencyById(id);
    }

    @GetMapping("/currency")
    public Page<Currency> getAllCurrencies(
            @SortDefault(sort = "id", direction = Sort.Direction.ASC) Pageable pageable, 
            @RequestParam(value = "order", defaultValue = "asc") String order) {
        String atrbt="";
        for (Sort.Order ordery : pageable.getSort()){
            atrbt = ordery.getProperty();
            }
            System.out.println("->" + atrbt +" - " +order);
        if (order.equals("desc")){
            pageable = PageRequest.of(pageable.getPageNumber(), 10, Sort.by(atrbt).descending()); 
        }
        return currencyservice.getCurrencies(pageable);
    }

    @PostMapping("/currency")
    public Currency createCurrencies(@RequestBody Currency currency) {
        return currencyservice.saveCurrency(currency);
    }

    @DeleteMapping("/currency/del/{id}")
    public String deleteCurrency(@PathVariable int id) {
        return currencyservice.deleteCurrency(id);
    }

    @GetMapping("/currency/{id}")
    public Page<Market> getMarketsByCurrencyId(@PathVariable(value = "id") int currencyId, @SortDefault(sort = "id", direction = Sort.Direction.ASC) Pageable pageable, @RequestParam(value = "order", defaultValue = "asc") String order) {
        String atrbt="";
        for (Sort.Order ordery : pageable.getSort()){
            atrbt = ordery.getProperty();
        }
        switch(atrbt){
            case "1":
                atrbt="origin_currency_id";
                break;
            case "2":
                atrbt="price";
                break;
            case "id":
                atrbt="id";
                break;
            default:
                System.out.println("Unexpected sortable property, using id");
                atrbt="id";
        }
        System.out.println("->" + atrbt +" - " +order);
        if (order.equals("desc")){
            pageable = PageRequest.of(pageable.getPageNumber(), 10, Sort.by(atrbt).descending()); 
        }else{
            pageable = PageRequest.of(pageable.getPageNumber(), 10, Sort.by(atrbt).ascending()); 
        }
        return marketservice.getMarketsByCurrency(currencyId, pageable);
    }

    @GetMapping("/currency/search")
    public List<Currency> getCurrenciesByName(@RequestParam String name) {
        return currencyservice.getCurrenciesByName(name);
    }

    //////////////////////////////////////////// EXTENSION
    //////////////////////////////////////////// ////////////////////////////////////////////

    @GetMapping("/extension")
    public Page<Extension> getAllExtensions(Pageable pageable) {
        return extensionservice.getExtensions(pageable);
    }

    @GetMapping("/extensions")
    public List<Extension> getExtensionsList() {
        return extensionservice.getExtensionsList();
    }

    @GetMapping("/extension/{id}")
    public Page<Extension> getUserExtensions(
            @PathVariable int id, 
            @SortDefault(sort = "id", direction = Sort.Direction.ASC) Pageable pageable, 
            @RequestParam(value = "order", defaultValue = "asc") String order) {
        //User user = userService.getUserById(id);
        String atrbt="";
        for (Sort.Order ordery : pageable.getSort()){
            atrbt = ordery.getProperty();
            }
            //System.out.println("->" + atrbt +" - " +order);
        if (order.equals("desc")){
            pageable = PageRequest.of(pageable.getPageNumber(), 10, Sort.by(atrbt).descending()); 
        }
        return extensionservice.getExtensionsByUser(id, pageable);
    }
 
    @PostMapping("/extension")
    public Extension createExtensions(@RequestParam int userId, @RequestParam String name,
            @RequestParam String description, @RequestParam String path) {
        User user = userService.getUserById(userId);
        Extension extension = new Extension(user, name, description, path);
        return extensionservice.saveExtension(extension);
    }

    @DeleteMapping("/extension/{id}")
    public String deleteExtensions(@PathVariable int id) {
        return extensionservice.deleteExtension(id);
    }

    //////////////////////////////////////////// PORTFOLIO
    //////////////////////////////////////////// ////////////////////////////////////////////

    // receber todos os portfolios de um user ID (tem de ter um parametro id!)
    @GetMapping("/portfolio")
    public List<Portfolio> getAllPortfolios(@RequestParam int id) {
        return portfolioservice.getPortfoliosbyUserID(id);
    }

    @DeleteMapping("/portfolio/{id}")
    public void deletePortfolios(@PathVariable int id, @RequestParam int user_id) {

        System.out.println("deleting portfolio..." + id);

        Portfolio p = portfolioservice.getPortfolioById(id);

        User u = service.getUserById(user_id);

        u.removePortfolio(p);

        p.removeUser(u);

        portfolioservice.savePortfolio(p);

        userService.saveUser(u);

    }

    // recebe um post do angular com os parametros name e user (maybe)
    @PostMapping("/portfolio")
    public Portfolio createPortfolio(@RequestParam String name, @RequestParam int id) {

        Portfolio p = portfolioservice.savePortfolio(new Portfolio(name));

        User u = service.getUserById(id);
        u.addPortfolio(p);
        userService.saveUser(u);

        Market m = marketservice.getMarketById(random.nextInt(marketservice.getMarketsCount().intValue() + 1));
        Order origin_order = orderservice
                .saveOrder(new Order(Float.valueOf(-100), Float.valueOf(0), systemPortfolio, m));
        Order destiny_order = orderservice.saveOrder(new Order(Float.valueOf(100), Float.valueOf(0), p, m));

        transactionservice.saveTransaction(new Transaction(origin_order, destiny_order));

        return p;

    }

    @PostMapping("/portfolio/join")
    public Portfolio joinPortfolio(@RequestParam String publicKey, @RequestParam int userId) {

        System.out.println(">> A juntar-se ao Portfolio '" + publicKey + "'...");
        Portfolio p = portfolioservice.getPortfoliobyPublicKey(publicKey);
        User u = service.getUserById(userId);
        u.addPortfolio(p);
        userService.saveUser(u);
        return p;

    }

    @GetMapping("/portfolio/{id}")
    public Portfolio getPortfolio(@PathVariable int id) {
        return portfolioservice.getPortfolioById(id);
    }

    @GetMapping("/portfolio/{id}/details")
    public Page<PCurrency> getPortfolioDetails(@PathVariable int id,@SortDefault(sort = "id", direction = Sort.Direction.ASC) Pageable pageable, @RequestParam(value = "order", defaultValue = "asc") String order) {
        String atrbt="";
        for (Sort.Order ordery : pageable.getSort()){
            atrbt = ordery.getProperty();
            }
            //System.out.println("->" + atrbt +" - " +order);
        if (order.equals("desc")){
            pageable = PageRequest.of(pageable.getPageNumber(), 10, Sort.by(atrbt).descending()); 
        }
        return portfolioservice.getPortfolioDetailsById(id, pageable);
    }

    @GetMapping("/portfolio/{id}/transactions")
    public Page<Map<String, Object>> getPortfolioTransactions(@PathVariable int id, Pageable pageable) {
        return transactionservice.getTransactionDetails(id, pageable);
    }

    @PostMapping("porfolio/users")
    public List<User> getPortfolioUsers(@RequestParam String publicKey) {
        System.out.println("/users do portfolio");
        return portfolioservice.getPortfolioByUsers(publicKey);
    }

    @GetMapping("/portfolio/extension/{id}")
    public List<Extension> getPortfolioExtensions(@PathVariable int id) {
        return portfolioservice.getPortfolioExtensions(id);
    }

    @PostMapping("/portfolio/extension")
    public void addExtension(@RequestParam int id, @RequestParam String path) {
        Extension extension = extensionservice.getExtensionByPath(path);
        Portfolio portfolio = portfolioservice.getPortfolioById(id);
        portfolio.addExtension(extension);
        portfolioservice.savePortfolio(portfolio);
        System.out.println("added extension " + extension.getPath() + " to portfolio " + portfolio.getName());
    }

    @DeleteMapping("/portfolio/extension")
    public void deletePortfolioExtensions(@RequestParam int id, @RequestParam String path) {

        Extension extension = extensionservice.getExtensionByPath(path);
        Portfolio portfolio = portfolioservice.getPortfolioById(id);
        portfolio.removeExtension(extension);
        portfolioservice.savePortfolio(portfolio);
    }

    //////////////////////////////////////////// MARKET
    //////////////////////////////////////////// ////////////////////////////////////////////

    @PostMapping("/market")
    public Market createMarkets(@RequestBody Market market) {
        return marketservice.saveMarket(market);
    }

    @GetMapping("/market")
    public Page<Market> getAllMarkets(@SortDefault(sort = "id", direction = Sort.Direction.ASC) Pageable pageable, @RequestParam(value = "order", defaultValue = "asc") String order) {
        String atrbt="";
        for (Sort.Order ordery : pageable.getSort()){
            atrbt = ordery.getProperty();
        }
        switch(atrbt){
            case "1":
                atrbt="origin_currency_id";
                break;
            case "2":
                atrbt="price";
                break;
            case "id":
                atrbt="id";
                break;
            default:
                System.out.println("Unexpected sortable property, using id");
                atrbt="id";
        }
        System.out.println("->" + atrbt +" - " +order);
        if (order.equals("desc")){
            pageable = PageRequest.of(pageable.getPageNumber(), 10, Sort.by(atrbt).descending()); 
        }else{
            pageable = PageRequest.of(pageable.getPageNumber(), 10, Sort.by(atrbt).ascending()); 
        }
        return marketservice.getMarkets(pageable);
    }

    // EndPoint para os gráficos
    @GetMapping("/market/{id}")
    public Map<String, Object> getTickersByMarketId(@PathVariable int id) {
        Market market = marketservice.getMarketById(id);
        List<Ticker> tickers = tickerservice.getTickerByMarket(market);

        Map<String, Object> marketSerialized = new HashMap<>();
        marketSerialized.put("id", market.getId());
        marketSerialized.put("originCurrency", market.getOriginCurrency());
        marketSerialized.put("destinyCurrency", market.getDestinyCurrency());
        marketSerialized.put("price", market.getPrice());
        marketSerialized.put("hourChange", market.getHourChange());
        marketSerialized.put("minuteChange", market.getMinuteChange());
        marketSerialized.put("tickers", tickers);

        return marketSerialized;
    }

    @GetMapping("/market/{id}/orders/sell")
    public Page<Order> getMarketSellOrders(@PathVariable int id, Pageable pageable) {
        return orderservice.getSellOrdersByMarket(id, pageable);
    }

    @GetMapping("/market/{id}/orders/buy")
    public Page<Order> getMarketBuyOrders(@PathVariable int id, Pageable pageable) {
        return orderservice.getBuyOrdersByMarket(id, pageable);
    }

    //////////////////////////////////////////// ORDER
    //////////////////////////////////////////// ////////////////////////////////////////////

    @GetMapping("/order")
    public Page<Order> getAllOrders(Pageable pageable) {
        return orderservice.getOrders(pageable);
    }

    @PostMapping("/order")
    public Order createOrders(@RequestParam int marketId, @RequestParam int portfolioId, @RequestParam Float quantity,
            @RequestParam Float orderValue) {
        if (quantity == 0)
            return null;

        Market market = marketservice.getMarketById(marketId);
        Currency req_curr;
        if (quantity > 0)
            req_curr = market.getOriginCurrency();
        else
            req_curr = market.getDestinyCurrency();

        PCurrency pcurr = portfolioservice.getCurrencyDetailsInPortfolio(portfolioId, req_curr.getId());
        if (pcurr == null)
            return null;

        if ((quantity > 0 && quantity > pcurr.getQuantity()) || (quantity < 0 && -quantity > pcurr.getQuantity()))
            return null;

        Order order = orderservice
                .saveOrder(new Order(quantity, orderValue, portfolioservice.getPortfolioById(portfolioId), market));
        if (order != null) {
            sendingOperations.convertAndSend("/order/" + market.getSymbol(), order);
            orderservice.checkClose(order);
        }
        return order;
    }

    @DeleteMapping("/order/{id}")
    public String deleteOrder(@PathVariable int id) {
        return orderservice.deleteOrder(id);
    }

    //////////////////////////////////////////// TRANSACTION
    //////////////////////////////////////////// ////////////////////////////////////////////

    @GetMapping("/transaction")
    public Page<Transaction> getAllTransactions(Pageable pageable) {
        return transactionservice.getTransactions(pageable);
    }

    @PostMapping("/transaction")
    public Transaction createTransaction(@RequestBody Transaction transaction) {
        return transactionservice.saveTransaction(transaction);
    }

    @DeleteMapping("/transaction/{id}")
    public String deleteTransaction(@PathVariable int id) {
        return transactionservice.deleteTransaction(id);
    }

    //////////////////////////////////////////// TICKER
    //////////////////////////////////////////// ////////////////////////////////////////////

    @GetMapping("/ticker")
    public Page<Ticker> getAllTickers(Pageable pageable) {
        return tickerservice.getTickers(pageable);
    }

    @PostMapping("/ticker")
    public Ticker createTicker(@RequestBody Ticker ticker) {
        return tickerservice.saveTicker(ticker);
    }

    @DeleteMapping("/ticker/{id}")
    public String deleteTicker(@PathVariable int id) {
        return tickerservice.deleteTicker(id);
    }

}