package yes.finance.services;

import yes.finance.model.Market;
import yes.finance.repository.MarketRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MarketService {
    @Autowired
    private MarketRepository repository;

    public Market saveMarket(Market market) {
        return repository.save(market);
    }

    public List<Market> saveMarkets(List<Market> markets) {
        return repository.saveAll(markets);
    }

    public List<Market> getMarkets() {
        return repository.findAll();
    }

    public Market getMarketById(int id) {
        return repository.findById((int)id).orElse(null);
    }

    // IMPLEMENTAR TODOS OS FIND's QUE TIVERMOS NO MarketRepository.java

    public String deleteMarket(int id) {
        repository.deleteById(id);
        return "Market (id="+ id +") removed!";
    }

    public Market updateMarket(Market market) {
        Market existingMarket = repository.findById((int)market.getId()).orElse(null);

        // FAZER SETS

        return saveMarket(existingMarket);
    }
}