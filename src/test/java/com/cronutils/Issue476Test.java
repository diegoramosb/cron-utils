package com.cronutils;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

import com.cronutils.model.CompositeCron;
import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;

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

		singleCron = cronParser.parse("0 0 10 ? * * *");

		compositeCron = new CompositeCron(Arrays.asList(cronParser.parse("0 0 10 ? * 2#1 *"),
														cronParser.parse("0 0 10 ? * 1#1 *")));
	}

	@Test
	public void overlapTest_whenCronExpressionsOverlap_shouldReturnTrue() {

		Cron overlappingCron = cronParser.parse("0 0 10 ? * MON *");

		assertTrue(singleCron.overlap(overlappingCron));
	}

	@Test
	public void overlapTest_whenCronExpressionsDoNotOverlap_shouldReturnFalse() {

		Cron nonOverlappingCron = cronParser.parse("0 0 11 ? * MON *");

		assertFalse(singleCron.overlap(nonOverlappingCron));
	}
}
