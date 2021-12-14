package yes.finance.repository;

import yes.finance.model.Market;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketRepository extends JpaRepository<Market,Integer> {
    Market findByID(int id);
}