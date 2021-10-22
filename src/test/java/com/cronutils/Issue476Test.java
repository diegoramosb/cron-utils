package com.cronutils;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

import com.cronutils.model.CompositeCron;
import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;

import com.cronutils.model.field.expression.FieldExpression;
import com.cronutils.model.field.expression.On;
import com.cronutils.model.field.value.IntegerFieldValue;
import com.cronutils.parser.CronParser;
import org.junit.Before;
import org.junit.Test;

public class Issue476Test {

	Cron singleCron;

	CompositeCron compositeCron;

	CronParser cronParser;

	@Before
	public void setUp() {

		final CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ);

		cronParser = new CronParser(cronDefinition);

		singleCron = cronParser.parse("0 0 10 ? * * 2021");

		compositeCron = new CompositeCron(Arrays.asList(cronParser.parse("0 0 10 ? * 2#1 *"),
														cronParser.parse("0 0 10 ? * 1#1 *")));
	}

	@Test
	public void yearsOverlapTest_whenOnYearsDoNotOverlap_shouldReturnFalse() {

		Cron cron1 = cronParser.parse("0 0 10 ? * * 2021");
		Cron cron2 = cronParser.parse("0 0 10 ? * * 2022");

		assertFalse(cron1.yearsOverlap(cron2));
	}

	@Test
	public void yearsOverlapTest_whenOnYearsOverlap_shouldReturnTrue() {

		Cron cron1 = cronParser.parse("0 0 10 ? * * 2021");
		Cron cron2 = cronParser.parse("0 0 0 1 JAN ? 2021");

		assertTrue(cron1.yearsOverlap(cron2));
	}

	@Test
	public void yearsOverlapTest_whenOnAndEveryOverlap_shouldReturnTrue() {

		Cron cron1 = cronParser.parse("0 0 0 1 JAN ? 2061");
		Cron cron2 = cronParser.parse("0 0 0 1 JAN ? 2021/5");

		assertTrue(cron1.yearsOverlap(cron2));
	}

	@Test
	public void yearsOverlapTest_whenOnAndEveryDoNotOverlap_shouldReturnFalse() {

		Cron cron1 = cronParser.parse("0 0 0 1 JAN ? 2062");
		Cron cron2 = cronParser.parse("0 0 0 1 JAN ? 2021/3");

		assertFalse(cron1.yearsOverlap(cron2));
	}

	@Test
	public void yearsOverlapTest_whenOnBeforeStartYear_shouldReturnFalse() {

		Cron cron1 = cronParser.parse("0 0 0 1 JAN ? 2020");
		Cron cron2 = cronParser.parse("0 0 0 1 JAN ? 2021/1");

		assertFalse(cron1.yearsOverlap(cron2));
	}

	@Test
	public void yearsOverlapTest_whenOnAndBetweenOverlap_shouldReturnTrue() {

		Cron cron1 = cronParser.parse("0 0 0 1 JAN ? 2021");
		Cron cron2 = cronParser.parse("0 0 0 1 JAN ? 2021-2023");

		assertTrue(cron1.yearsOverlap(cron2));
	}

	@Test
	public void yearsOverlapTest_whenYearBeforeRange_shouldReturnFalse() {

		Cron cron1 = cronParser.parse("0 0 0 1 JAN ? 2021");
		Cron cron2 = cronParser.parse("0 0 0 1 JAN ? 2022-2023");

		assertFalse(cron1.yearsOverlap(cron2));
	}

//	@Test
//	public void yearsOverlapTest_whenBetweenAndOnOverlap_shouldReturnTrue() {
//
//		Cron cron1 = cronParser.parse("0 0 0 1 JAN ? 2021-2023");
//		Cron cron2 = cronParser.parse("0 0 0 1 JAN ? 2021");
//
//		assertTrue(cron1.yearsOverlap(cron2));
//	}
//
	@Test
	public void yearsOverlapTest_whenYearAfterRange_shouldReturnFalse() {

		Cron cron1 = cronParser.parse("0 0 0 1 JAN ? 2024");
		Cron cron2 = cronParser.parse("0 0 0 1 JAN ? 2022-2023");

		assertFalse(cron1.yearsOverlap(cron2));
	}

	@Test
	public void yearsOverlapTest_whenOnInList_shouldReturnTrue() {

		Cron cron1 = cronParser.parse("0 0 0 1 JAN ? 2021");
		Cron cron2 = cronParser.parse("0 0 0 1 JAN ? 2019,2021,2022");

		assertTrue(cron1.yearsOverlap(cron2));
	}

	@Test
	public void yearsOverlapTest_whenEveryAndOnOverlap_shouldReturnTrue() {

		Cron cron1 = cronParser.parse("0 0 0 1 JAN ? 2021/5");
		Cron cron2 = cronParser.parse("0 0 0 1 JAN ? 2061");

		assertTrue(cron1.yearsOverlap(cron2));
	}

	@Test
	public void yearsOverlapTest_whenEveryAndOnNotOverlap_shouldReturnFalse() {

		Cron cron1 = cronParser.parse("0 0 0 1 JAN ? 2021/5");
		Cron cron2 = cronParser.parse("0 0 0 1 JAN ? 2062");

		assertFalse(cron1.yearsOverlap(cron2));
	}

	@Test
	public void yearsOverlapTest_whenEveryAndOnNotOverlapOnBeforeStartYear_shouldReturnFalse() {

		Cron cron1 = cronParser.parse("0 0 0 1 JAN ? 2021/5");
		Cron cron2 = cronParser.parse("0 0 0 1 JAN ? 2020");

		assertFalse(cron1.yearsOverlap(cron2));
	}

	@Test
	public void yearsOverlapTest_whenEveryAndEveryOverlap_shouldReturnTrue() {

		Cron cron1 = cronParser.parse("0 0 0 1 JAN ? 2021/5");
		Cron cron2 = cronParser.parse("0 0 0 1 JAN ? 2022/7");

		assertTrue(cron1.yearsOverlap(cron2));
	}

	@Test
	public void yearsOverlapTest_whenEveryAndEveryNotOverlap_shouldReturnFalse() {

		Cron cron1 = cronParser.parse("0 0 0 1 JAN ? 2021/3");
		Cron cron2 = cronParser.parse("0 0 0 1 JAN ? 2020/3");

		assertFalse(cron1.yearsOverlap(cron2));
	}

	@Test
	public void yearsOverlapTest_whenEveryAndBetweenOverlap_shouldReturnTrue() {

		Cron cron1 = cronParser.parse("0 0 0 1 JAN ? 2021/5");
		Cron cron2 = cronParser.parse("0 0 0 1 JAN ? 2021-2023");

		assertTrue(cron1.yearsOverlap(cron2));
	}

	@Test
	public void yearsOverlapTest_whenEveryAndBetweenNotOverlap_shouldReturnFalse() {

		Cron cron1 = cronParser.parse("0 0 0 1 JAN ? 2021/10");
		Cron cron2 = cronParser.parse("0 0 0 1 JAN ? 2022-2029");

		assertFalse(cron1.yearsOverlap(cron2));
	}

	@Test
	public void yearsOverlapTest_whenEveryInList_shouldReturnTrue() {

		Cron cron1 = cronParser.parse("0 0 0 1 JAN ? 2021/10");
		Cron cron2 = cronParser.parse("0 0 0 1 JAN ? 2020,2031,2045");

		assertTrue(cron1.yearsOverlap(cron2));
	}

	@Test
	public void yearsOverlapTest_whenEveryNotInList_shouldReturnFalse() {

		Cron cron1 = cronParser.parse("0 0 0 1 JAN ? 2021/10");
		Cron cron2 = cronParser.parse("0 0 0 1 JAN ? 2020,2030,2045");

		assertFalse(cron1.yearsOverlap(cron2));
	}

//	@Test
//	public void yearsOverlapTest_whenOnInListReverse_shouldReturnTrue() {
//
//		Cron cron1 = cronParser.parse("0 0 0 1 JAN ? 2019,2021,2022");
//		Cron cron2 = cronParser.parse("0 0 0 1 JAN ? 2021");
//
//		assertTrue(cron1.yearsOverlap(cron2));
//	}

	@Test
	public void yearsOverlapTest_whenOnNotInList_shouldReturnTrue() {

		Cron cron1 = cronParser.parse("0 0 0 1 JAN ? 2021");
		Cron cron2 = cronParser.parse("0 0 0 1 JAN ? 2019,2022,2023");

		assertFalse(cron1.yearsOverlap(cron2));
	}

	//	@Test
//	public void yearsOverlapTest_whenBetweenYearsDoNotOverlap_shouldReturnFalse() {
//
//		Cron cron1 = cronParser.parse("0 0 10 ? * * 2021-2022");
//		Cron cron2 = cronParser.parse("0 0 10 ? * * 2023-2024");
//
//		assertFalse(cron1.yearsOverlap(cron2));
//	}
//
//	@Test
//	public void yearsOverlapTest_whenBetweenYearsOverlap_shouldReturnTrue() {
//
//		Cron cron1 = cronParser.parse("0 0 0 1 JAN ? 2020,2032,2033");
//		Cron cron2 = cronParser.parse("0 0 10 ? * * 2022-2023");
//
//		assertTrue(cron1.yearsOverlap(cron2));
//	}

//	@Test
//	public void yearsOverlapTest_whenSingleYearsDoNotOverlap_shouldReturnFalse() {
//
//	}
//
//	@Test
//	public void overlapTest_whenMonthsDoNotOverlap_shouldReturnFalse() {
//
//	}
//
//	@Test
//	public void overlapTest_whenCronExpressionsOverlap_shouldReturnTrue() {
//
//		Cron overlappingCron = cronParser.parse("0 0 10 ? * MON *");
//
//		assertTrue(singleCron.overlap(overlappingCron));
//	}
//
//	@Test
//	public void overlapTest_whenCronExpressionsDoNotOverlap_shouldReturnFalse() {
//
//		Cron nonOverlappingCron = cronParser.parse("0 0 11 ? * MON *");
//
//		assertFalse(singleCron.overlap(nonOverlappingCron));
//	}
}
