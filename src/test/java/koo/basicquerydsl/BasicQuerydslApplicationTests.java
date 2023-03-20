package koo.basicquerydsl;

import koo.basicquerydsl.entity.Member;
import koo.basicquerydsl.entity.Team;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;


@SpringBootTest
class BasicQuerydslApplicationTests {

	@Test
	void contextLoads() {
	}

}
