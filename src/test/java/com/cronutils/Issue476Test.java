package com.cronutils;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigInteger;
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
import com.cronutils.utils.SequenceUtils;
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

	@Test
	public void yearsOverlapTest_whenOnNotInList_shouldReturnFalse() {

		Cron cron1 = cronParser.parse("0 0 0 1 JAN ? 2021");
		Cron cron2 = cronParser.parse("0 0 0 1 JAN ? 2019,2022,2023");

		assertFalse(cron1.yearsOverlap(cron2));
	}


	@Test
	public void yearsOverlapTest_whenBetweenAndOnOverlap_shouldReturnTrue() {

		Cron cron1 = cronParser.parse("0 0 0 1 JAN ? 2021-2023");
		Cron cron2 = cronParser.parse("0 0 0 1 JAN ? 2021");

		assertTrue(cron1.yearsOverlap(cron2));
	}

	@Test
	public void yearsOverlapTest_whenBetweenAndOnNotOverlap_shouldReturnFalse() {

		Cron cron1 = cronParser.parse("0 0 0 1 JAN ? 2021-2023");
		Cron cron2 = cronParser.parse("0 0 0 1 JAN ? 2024");

		assertFalse(cron1.yearsOverlap(cron2));
	}


	@Test
	public void yearsOverlapTest_whenBetweenAndEveryOverlap_shouldReturnTrue() {

		Cron cron1 = cronParser.parse("0 0 0 1 JAN ? 2021-2023");
		Cron cron2 = cronParser.parse("0 0 0 1 JAN ? 2021/5");

		assertTrue(cron1.yearsOverlap(cron2));
	}

	@Test
	public void yearsOverlapTest_whenBetweenAndEveryNotOverlap_shouldReturnFalse() {

		Cron cron1 = cronParser.parse("0 0 0 1 JAN ? 2022-2029");
		Cron cron2 = cronParser.parse("0 0 0 1 JAN ? 2021/10");

		assertFalse(cron1.yearsOverlap(cron2));
	}

	@Test
	public void yearsOverlapTest_whenBetweenAndBetweenOverlap_shouldReturnTrue() {

		Cron cron1 = cronParser.parse("0 0 0 1 JAN ? 2021-2023");
		Cron cron2 = cronParser.parse("0 0 0 1 JAN ? 2023-2024");

		assertTrue(cron1.yearsOverlap(cron2));
	}

	@Test
	public void yearsOverlapTest_whenBetweenAndBetweenNotOverlap_shouldReturnFalse() {

		Cron cron1 = cronParser.parse("0 0 0 1 JAN ? 2021-2023");
		Cron cron2 = cronParser.parse("0 0 0 1 JAN ? 2024-2025");

		assertFalse(cron1.yearsOverlap(cron2));
	}

	@Test
	public void yearsOverlapTest_whenBetweenAndListOverlap_shouldReturnTrue() {

		Cron cron1 = cronParser.parse("0 0 0 1 JAN ? 2021-2023");
		Cron cron2 = cronParser.parse("0 0 0 1 JAN ? 2022,2024,2025");

		assertTrue(cron1.yearsOverlap(cron2));
	}

	@Test
	public void yearsOverlapTest_whenBetweenAndListNotOverlap_shouldReturnFalse() {

		Cron cron1 = cronParser.parse("0 0 0 1 JAN ? 2021-2023");
		Cron cron2 = cronParser.parse("0 0 0 1 JAN ? 2024,2025");

		assertFalse(cron1.yearsOverlap(cron2));
	}

	@Test
	public void yearsOverlapTest_whenListAndOnOverlap_shouldReturnTrue() {

		Cron cron1 = cronParser.parse("0 0 0 1 JAN ? 2019,2021,2022");
		Cron cron2 = cronParser.parse("0 0 0 1 JAN ? 2021");

		assertTrue(cron1.yearsOverlap(cron2));
	}

	@Test
	public void yearsOverlapTest_whenListAndOnNotOverlap_shouldReturnFalse() {

		Cron cron1 = cronParser.parse("0 0 0 1 JAN ? 2019,2022,2023");
		Cron cron2 = cronParser.parse("0 0 0 1 JAN ? 2021");

		assertFalse(cron1.yearsOverlap(cron2));
	}

	@Test
	public void yearsOverlapTest_whenListAndEveryOverlap_shouldReturnTrue() {

		Cron cron1 = cronParser.parse("0 0 0 1 JAN ? 2020,2031,2045");
		Cron cron2 = cronParser.parse("0 0 0 1 JAN ? 2021/10");

		assertTrue(cron1.yearsOverlap(cron2));
	}

	@Test
	public void yearsOverlapTest_whenListAndEveryNotOverlap_shouldReturnFalse() {

		Cron cron1 = cronParser.parse("0 0 0 1 JAN ? 2020,2030,2045");
		Cron cron2 = cronParser.parse("0 0 0 1 JAN ? 2021/10");

		assertFalse(cron1.yearsOverlap(cron2));
	}

	@Test
	public void yearsOverlapTest_whenListAndBetweenOverlap_shouldReturnTrue() {

		Cron cron1 = cronParser.parse("0 0 0 1 JAN ? 2022,2024,2025");
		Cron cron2 = cronParser.parse("0 0 0 1 JAN ? 2021-2023");

		assertTrue(cron1.yearsOverlap(cron2));
	}

	@Test
	public void yearsOverlapTest_whenListAndBetweenNotOverlap_shouldReturnFalse() {

		Cron cron1 = cronParser.parse("0 0 0 1 JAN ? 2024,2025");
		Cron cron2 = cronParser.parse("0 0 0 1 JAN ? 2021-2023");

		assertFalse(cron1.yearsOverlap(cron2));
	}

	@Test
	public void yearsOverlapTest_whenTwoListsOverlap_shouldReturnTrue() {

		Cron cron1 = cronParser.parse("0 0 0 1 JAN ? 2024,2022,2027");
		Cron cron2 = cronParser.parse("0 0 0 1 JAN ? 2025,2030,2024");

		assertTrue(cron1.yearsOverlap(cron2));
	}

	@Test
	public void yearsOverlapTest_whenTwoListsNotOverlap_shouldReturnFalse() {

		Cron cron1 = cronParser.parse("0 0 0 1 JAN ? 2024,2022,2027");
		Cron cron2 = cronParser.parse("0 0 0 1 JAN ? 2025,2030,2025");

		assertFalse(cron1.yearsOverlap(cron2));
	}

	@Test
	public void monthsOverlapTest_whenOnAndOnOverlap_shouldReturnTrue() {

		Cron cron1 = cronParser.parse("0 0 0 1 JAN ? 2021");
		Cron cron2 = cronParser.parse("0 0 0 1 JAN ? 2021");

		assertTrue(cron1.monthsOverlap(cron2));
	}

	@Test
	public void monthsOverlapTest_whenOnAndOnNotOverlap_shouldReturnFalse() {

		Cron cron1 = cronParser.parse("0 0 0 1 JAN ? 2021");
		Cron cron2 = cronParser.parse("0 0 0 1 FEB ? 2021");

		assertFalse(cron1.monthsOverlap(cron2));
	}

	@Test
	public void monthsOverlapTest_whenOnAndEveryOverlapEveryYear_shouldReturnTrue() {

		Cron cron1 = cronParser.parse("0 0 0 1 JAN ? 2022");
		Cron cron2 = cronParser.parse("0 0 0 1 2/1 ? 2021/1");

		assertTrue(cron1.monthsOverlap(cron2));
	}

	@Test
	public void monthsOverlapTest_whenOnAndEveryNotOverlapAnyYear_shouldReturnFalse() {

		Cron cron1 = cronParser.parse("0 0 0 1 JAN ? *");
		Cron cron2 = cronParser.parse("0 0 0 1 2/12 ? *");

		assertFalse(cron1.monthsOverlap(cron2));
	}

	@Test
	public void monthsOverlapTest_whenOnAndEveryOverlapSameYear_shouldReturnTrue() {

		Cron cron1 = cronParser.parse("0 0 0 1 AUG ? 2021");
		Cron cron2 = cronParser.parse("0 0 0 1 2/1 ? 2021");

		assertTrue(cron1.monthsOverlap(cron2));
	}

	@Test
	public void monthsOverlapTest_whenOnAndEveryNotOverlapSameYear_shouldReturnFalse() {

		Cron cron1 = cronParser.parse("0 0 0 1 JAN ? 2021");
		Cron cron2 = cronParser.parse("0 0 0 1 2/1 ? 2021");

		assertFalse(cron1.monthsOverlap(cron2));
	}

	@Test
	public void monthsOverlapTest_whenOnAndBetweenOverlap_shouldReturnTrue() {

		Cron cron1 = cronParser.parse("0 0 0 1 AUG ? 2021");
		Cron cron2 = cronParser.parse("0 0 0 1 6-8 ? 2021");

		assertTrue(cron1.monthsOverlap(cron2));
	}

	@Test
	public void monthsOverlapTest_whenOnAndBetweenNotOverlap_shouldReturnFalse() {

		Cron cron1 = cronParser.parse("0 0 0 1 JAN ? 2021");
		Cron cron2 = cronParser.parse("0 0 0 1 2-5 ? 2021");

		assertFalse(cron1.monthsOverlap(cron2));
	}

	@Test
	public void monthsOverlapTest_whenOnAndListOverlap_shouldReturnTrue() {

		Cron cron1 = cronParser.parse("0 0 0 1 AUG ? 2021");
		Cron cron2 = cronParser.parse("0 0 0 1 JUL,AUG,SEP ? 2021");

		assertTrue(cron1.monthsOverlap(cron2));
	}

	@Test
	public void monthsOverlapTest_whenOnAndListNotOverlap_shouldReturnFalse() {

		Cron cron1 = cronParser.parse("0 0 0 1 JAN ? 2021");
		Cron cron2 = cronParser.parse("0 0 0 1 FEB,MAR ? 2021");

		assertFalse(cron1.monthsOverlap(cron2));
	}

	@Test
	public void monthsOverlapTest_whenEveryAndOnOverlap_shouldReturnTrue() {

		Cron cron1 = cronParser.parse("0 0 0 1 2/1 ? *");
		Cron cron2 = cronParser.parse("0 0 0 1 JAN ? *");

		assertTrue(cron1.monthsOverlap(cron2));
	}

	@Test
	public void monthsOverlapTest_whenEveryAndOnNotOverlap_shouldReturnFalse() {

		Cron cron1 = cronParser.parse("0 0 0 1 2/12 ? 2021/1");
		Cron cron2 = cronParser.parse("0 0 0 1 JAN ? 2022-2023");

		assertFalse(cron1.monthsOverlap(cron2));
	}

	@Test
	public void monthsOverlapTest_whenEveryAndEveryOverlap_shouldReturnTrue() {

		Cron cron1 = cronParser.parse("0 0 0 1 3/3 ? *");
		Cron cron2 = cronParser.parse("0 0 0 1 2/5 ? *");

		assertTrue(cron1.monthsOverlap(cron2));
	}

	@Test
	public void monthsOverlapTest_whenEveryAndEveryDoNotOverlap_shouldReturnFalse() {

		Cron cron1 = cronParser.parse("0 0 0 1 1/3 ? *");
		Cron cron2 = cronParser.parse("0 0 0 1 2/3 ? *");

		assertFalse(cron1.monthsOverlap(cron2));
	}

	@Test
	public void monthsOverlapTest_whenListAndListOverlap_shouldReturnTrue() {

		Cron cron1 = cronParser.parse("0 0 0 1 JAN,APR,JUN ? *");
		Cron cron2 = cronParser.parse("0 0 0 1 FEB,JUN,JUL ? *");

		assertTrue(cron1.monthsOverlap(cron2));
	}

	@Test
	public void monthsOverlapTest_whenListAndListDoNotOverlap_shouldReturnFalse() {

		Cron cron1 = cronParser.parse("0 0 0 1 JAN,APR,JUN ? *");
		Cron cron2 = cronParser.parse("0 0 0 1 FEB,MAR,JUL ? *");

		assertFalse(cron1.monthsOverlap(cron2));
	}

	@Test
	public void monthsOverlapTest_whenListAndOnOverlap_shouldReturnTrue() {

		Cron cron1 = cronParser.parse("0 0 0 1 JAN,APR,JUN ? *");
		Cron cron2 = cronParser.parse("0 0 0 1 APR ? *");

		assertTrue(cron1.monthsOverlap(cron2));
	}

	@Test
	public void monthsOverlapTest_whenListAndOnDoNotOverlap_shouldReturnFalse() {

		Cron cron1 = cronParser.parse("0 0 0 1 JAN,APR,JUN ? *");
		Cron cron2 = cronParser.parse("0 0 0 1 MAR ? *");

		assertFalse(cron1.monthsOverlap(cron2));
	}

	@Test
	public void monthsOverlapTest_whenListAndEveryOverlap_shouldReturnTrue() {

		Cron cron1 = cronParser.parse("0 0 0 1 JAN,APR,JUN ? *");
		Cron cron2 = cronParser.parse("0 0 0 1 2/1 ? *");

		assertTrue(cron1.monthsOverlap(cron2));
	}

	@Test
	public void monthsOverlapTest_whenListAndEveryDoNotOverlap_shouldReturnFalse() {

		Cron cron1 = cronParser.parse("0 0 0 1 JAN,APR,JUN ? 2021");
		Cron cron2 = cronParser.parse("0 0 0 1 7/1 ? 2021");

		assertFalse(cron1.monthsOverlap(cron2));
	}

	@Test
	public void monthsOverlapTest_whenListAndBetweenOverlap_shouldReturnTrue() {

		Cron cron1 = cronParser.parse("0 0 0 1 JAN,APR,JUN ? *");
		Cron cron2 = cronParser.parse("0 0 0 1 5-6 ? *");

		assertTrue(cron1.monthsOverlap(cron2));
	}

	@Test
	public void monthsOverlapTest_whenListAndBetweenDoNotOverlap_shouldReturnFalse() {

		Cron cron1 = cronParser.parse("0 0 0 1 JAN,APR,JUL ? 2021");
		Cron cron2 = cronParser.parse("0 0 0 1 5-6 ? 2021");

		assertFalse(cron1.monthsOverlap(cron2));
	}

	@Test
	public void monthsOverlapTest_whenBetweenAndEveryOverlap_shouldReturnTrue() {

		Cron cron1 = cronParser.parse("0 0 0 1 1-2 ? *");
		Cron cron2 = cronParser.parse("0 0 0 1 3/11 ? *");

		assertTrue(cron1.monthsOverlap(cron2));
	}

	@Test
	public void monthsOverlapTest_whenBetweenAndEveryDoNotOverlap_shouldReturnFalse() {

		Cron cron1 = cronParser.parse("0 0 0 1 1-2 ? *");
		Cron cron2 = cronParser.parse("0 0 0 1 3/12 ? *");

		assertFalse(cron1.monthsOverlap(cron2));
	}


	@Test
	public void daysOverlapTest_whenDoMNthOnAndOnOverlap_shouldReturnTrue() {

		Cron cron1 = cronParser.parse("0 0 0 L-5 * ? *");
		Cron cron2 = cronParser.parse("0 0 0 L-5 * ? *");

		assertTrue(cron1.daysOverlap(cron2));
	}

	@Test
	public void daysOverlapTest_whenDoMNthOnAndOnDoNotOverlap_shouldReturnFalse() {

		Cron cron1 = cronParser.parse("0 0 0 L-2 * ? *");
		Cron cron2 = cronParser.parse("0 0 0 L-5 * ? *");

		assertFalse(cron1.daysOverlap(cron2));
	}

	@Test
	public void daysOverlapTest_whenDoMOnAndOnOverlap_shouldReturnTrue() {

		Cron cron1 = cronParser.parse("0 0 0 10 * ? *");
		Cron cron2 = cronParser.parse("0 0 0 10 * ? *");

		assertTrue(cron1.daysOverlap(cron2));
	}

	@Test
	public void daysOverlapTest_whenDoMOnAndOnDoNotOverlap_shouldReturnFalse() {

		Cron cron1 = cronParser.parse("0 0 0 20 * ? *");
		Cron cron2 = cronParser.parse("0 0 0 15 * ? *");

		assertFalse(cron1.daysOverlap(cron2));
	}

	@Test
	public void daysOverlapTest_whenDoMWeekdayOnAndOnOverlap_shouldReturnTrue() {

		Cron cron1 = cronParser.parse("0 0 0 10W * ? *");
		Cron cron2 = cronParser.parse("0 0 0 10W * ? *");

		assertTrue(cron1.daysOverlap(cron2));
	}

	@Test
	public void daysOverlapTest_whenDoMWeekdayOnAndOnDoNotOverlap_shouldReturnFalse() {

		Cron cron1 = cronParser.parse("0 0 0 20W * ? *");
		Cron cron2 = cronParser.parse("0 0 0 15W * ? *");

		assertFalse(cron1.daysOverlap(cron2));
	}

	@Test
	public void extendedEuclideanAlgorithmTest_whenQuotientNotZero_returnResult() {

		final Integer a = -12;
		final Integer b = 7;

		assertArrayEquals(new Integer[]{1, -3, -5}, SequenceUtils.extendedEuclideanAlgorithm(a, b));
	}


	@Test
	public void extendedEuclideanAlgorithmTest_whenQuotientZero_returnResult() {

		final Integer a = 5;
		final Integer b = 0;

		assertArrayEquals(new Integer[]{5, 1, 0}, SequenceUtils.extendedEuclideanAlgorithm(a, b));
	}


	@Test
	public void extendedEuclideanAlgorithmTest_whenDividendZero_returnResult() {

		final Integer a = 0;
		final Integer b = 3;

		assertArrayEquals(new Integer[]{3, 0, 1}, SequenceUtils.extendedEuclideanAlgorithm(a, b));
	}

	@Test
	public void seriesOverlapTest_whenSeriesOverlap_shouldReturnTrue() {

		final Integer a1 = 1;
		final BigInteger d = BigInteger.valueOf(2);
		final Integer b1 = 2;
		final BigInteger e = BigInteger.valueOf(3);
		final Integer gcd = d.gcd(e).intValue();

		assertTrue(SequenceUtils.seriesOverlap(a1, d.intValue(), b1, e.intValue(), gcd));
	}

	@Test
	public void seriesOverlapTest_whenSeriesDoNotOverlap_shouldReturnFalse() {

		final Integer a1 = 1;
		final BigInteger d = BigInteger.valueOf(0);
		final Integer b1 = 2;
		final BigInteger e = BigInteger.valueOf(1);
		final Integer gcd = d.gcd(e).intValue();

		assertFalse(SequenceUtils.seriesOverlap(a1, d.intValue(), b1, e.intValue(), gcd));
	}

	@Test
	public void firstOverlapTest_whenSeriesOverlap_shouldReturnFirstOverlap() {

		final Integer a1 = 1;
		final Integer d = 11;
		final Integer b1 = 2;
		final Integer e = 17;

		assertEquals(155, SequenceUtils.firstOverlap(a1, d ,b1, e));
	}

	@Test
	public void firstOverlapTest_whenSeriesDoNotOverlap_shouldReturnIntegerMaxValue() {

		final Integer a1 = 1;
		final Integer d = 10;
		final Integer b1 = 2;
		final Integer e = 10;

		assertEquals(Integer.MAX_VALUE, SequenceUtils.firstOverlap(a1, d ,b1, e));
	}


}
