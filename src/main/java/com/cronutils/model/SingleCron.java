package com.cronutils.model;

import com.cronutils.mapper.CronMapper;
import com.cronutils.model.definition.CronConstraint;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.field.CronField;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.expression.Always;
import com.cronutils.model.field.expression.And;
import com.cronutils.model.field.expression.Between;
import com.cronutils.model.field.expression.Every;
import com.cronutils.model.field.expression.FieldExpression;
import com.cronutils.model.field.expression.On;
import com.cronutils.model.field.expression.visitor.ValidationFieldExpressionVisitor;
import com.cronutils.model.field.value.IntegerFieldValue;
import com.cronutils.utils.Preconditions;
import com.cronutils.utils.SequenceUtils;

import java.util.*;

public class SingleCron implements Cron {

	private static final long serialVersionUID = 7487370826825439098L;
	private final CronDefinition cronDefinition;
	private final Map<CronFieldName, CronField> fields;
	private String asString;

	/**
	 * Creates a Cron with the iven cron definition and the given fields.
	 *
	 * @param cronDefinition the definition to use for this Cron
	 * @param fields         the fields that should be used
	 */
	public SingleCron(final CronDefinition cronDefinition, final List<CronField> fields) {

		this.cronDefinition = Preconditions.checkNotNull(cronDefinition, "CronDefinition must not be null");
		Preconditions.checkNotNull(fields, "CronFields cannot be null");
		this.fields = new EnumMap<>(CronFieldName.class);
		for (final CronField field : fields) {
			this.fields.put(field.getField(), field);
		}
	}

	/**
	 * Retrieve value for cron field.
	 *
	 * @param name - cron field name. If null, a NullPointerException will be raised.
	 * @return CronField that corresponds to given CronFieldName
	 */
	public CronField retrieve(final CronFieldName name) {

		return fields.get(Preconditions.checkNotNull(name, "CronFieldName must not be null"));
	}

	/**
	 * Retrieve all cron field values as map.
	 *
	 * @return unmodifiable Map with key CronFieldName and values CronField, never null
	 */
	public Map<CronFieldName, CronField> retrieveFieldsAsMap() {

		return Collections.unmodifiableMap(fields);
	}

	public String asString() {

		if (asString == null) {
			final ArrayList<CronField> temporaryFields = new ArrayList<>(fields.values());
			temporaryFields.sort(CronField.createFieldComparator());
			final StringBuilder builder = new StringBuilder();
			for (final CronField field : temporaryFields) {
				builder.append(String.format("%s ", field.getExpression().asString()));
			}
			asString = builder.toString().trim();
		}
		return asString;
	}

	public CronDefinition getCronDefinition() {

		return cronDefinition;
	}

	/**
	 * Validates this Cron instance by validating its cron expression.
	 *
	 * @return this Cron instance
	 * @throws IllegalArgumentException if the cron expression is invalid
	 */
	public Cron validate() {

		for (final Map.Entry<CronFieldName, CronField> field : retrieveFieldsAsMap().entrySet()) {
			final CronFieldName fieldName = field.getKey();
			field.getValue().getExpression().accept(
					new ValidationFieldExpressionVisitor(getCronDefinition().getFieldDefinition(fieldName).getConstraints())
												   );
		}
		for (final CronConstraint constraint : getCronDefinition().getCronConstraints()) {
			if (!constraint.validate(this)) {
				throw new IllegalArgumentException(
						String.format("Invalid cron expression: %s. %s", asString(), constraint.getDescription()));
			}
		}
		return this;
	}

	/**
	 * Provides means to compare if two cron expressions are equivalent.
	 *
	 * @param cronMapper - maps 'cron' parameter to this instance definition;
	 * @param cron       - any cron instance, never null
	 * @return boolean - true if equivalent; false otherwise.
	 */
	public boolean equivalent(final CronMapper cronMapper, final Cron cron) {

		return asString().equals(cronMapper.map(cron).asString());
	}

	/**
	 * Provides means to compare if two cron expressions are equivalent. Assumes same cron definition.
	 *
	 * @param cron - any cron instance, never null
	 * @return boolean - true if equivalent; false otherwise.
	 */
	public boolean equivalent(final Cron cron) {

		return asString().equals(cron.asString());
	}

	/**
	 * Checks if the cron expression overlaps with another.
	 *
	 * @param cron - any cron instance, never null
	 * @return boolean - true if the expression overlaps with another; false otherwise
	 */
	@Override
	public boolean overlap(final Cron cron) {

		boolean overlap = false;

		return overlap;
	}

	private boolean shouldTransformOnIntoEvery(final On toVerify, final FieldExpression firstHigherExpression,
									final FieldExpression secondHigherExpression) {

		boolean transform = false;
		if (Always.class.equals(firstHigherExpression.getClass()) || Always.class.equals(secondHigherExpression.getClass())) {
			transform = true;
		} else if (On.class.equals(firstHigherExpression.getClass()) || On.class.equals(secondHigherExpression.getClass())) {
			transform = Every.class.equals(firstHigherExpression.getClass()) || Every.class.equals(secondHigherExpression.getClass());
		}

		return transform;
	}

	public boolean daysOverlap(final Cron cron) {

		FieldExpression thisExpression = this.retrieve(CronFieldName.DAY_OF_MONTH).getExpression() != FieldExpression.questionMark() ?
										 this.retrieve(CronFieldName.DAY_OF_MONTH).getExpression() :
										 this.retrieve(CronFieldName.DAY_OF_WEEK).getExpression();
		FieldExpression otherExpression =
				cron.retrieve(CronFieldName.DAY_OF_MONTH).getExpression() != FieldExpression.questionMark() ?
				cron.retrieve(CronFieldName.DAY_OF_MONTH).getExpression() :
				cron.retrieve(CronFieldName.DAY_OF_WEEK).getExpression();

		boolean overlap = false;
		if (Always.class.equals(thisExpression.getClass()) || Always.class.equals(otherExpression.getClass())) {
			overlap = true;
		} else if (On.class.equals(thisExpression.getClass())) {
			overlap = onAndOtherOverlap((On) thisExpression, otherExpression);
		} else if (Every.class.equals(thisExpression.getClass())) {
			overlap = everyAndOtherOverlap((Every) thisExpression, otherExpression);
		} else if (Between.class.equals(thisExpression.getClass())) {
			final Integer start = (Integer) ((Between) thisExpression).getFrom().getValue();
			final Integer end = (Integer) ((Between) thisExpression).getTo().getValue();
			for (int i = start; i <= end && !overlap; i++) {
				final On currentExpression = new On((new IntegerFieldValue(i)));
				overlap = onAndOtherOverlap(currentExpression, otherExpression);
			}
		} else if (And.class.equals(thisExpression.getClass())) {
			final FieldExpression finalOtherExpression = otherExpression;
			overlap = ((And) thisExpression).getExpressions().stream()
											.anyMatch(expression -> onAndOtherOverlap((On) expression, finalOtherExpression));
		}

		return overlap;
	}

	public boolean monthsOverlap(final Cron cron) {

		FieldExpression thisExpression = this.retrieve(CronFieldName.MONTH).getExpression();
		FieldExpression otherExpression = cron.retrieve(CronFieldName.MONTH).getExpression();
		boolean overlap = false;
		if (On.class.equals(thisExpression.getClass()) && shouldTransformOnIntoEvery((On) thisExpression,
																					 this.retrieve(CronFieldName.YEAR).getExpression(),
																					 cron.retrieve(CronFieldName.YEAR).getExpression())) {
			thisExpression = new Every(thisExpression, new IntegerFieldValue(12));
		}
		if (On.class.equals(otherExpression.getClass()) && shouldTransformOnIntoEvery((On) otherExpression,
																					  this.retrieve(CronFieldName.YEAR).getExpression(),
																					  cron.retrieve(CronFieldName.YEAR).getExpression())) {
			otherExpression = new Every(otherExpression, new IntegerFieldValue(12));

		}

		if (Always.class.equals(thisExpression.getClass()) || Always.class.equals(otherExpression.getClass())) {
			overlap = true;
		} else if (On.class.equals(thisExpression.getClass())) {
			overlap = onAndOtherOverlap((On) thisExpression, otherExpression);
		} else if (Every.class.equals(thisExpression.getClass())) {
			overlap = everyAndOtherOverlap((Every) thisExpression, otherExpression);
		} else if (Between.class.equals(thisExpression.getClass())) {
			final Integer start = (Integer) ((Between) thisExpression).getFrom().getValue();
			final Integer end = (Integer) ((Between) thisExpression).getTo().getValue();
			for (int i = start; i <= end && !overlap; i++) {
				final On currentExpression = new On((new IntegerFieldValue(i)));
				if(shouldTransformOnIntoEvery(currentExpression, this.retrieve(CronFieldName.YEAR).getExpression(),
											  cron.retrieve(CronFieldName.YEAR).getExpression())) {
					overlap = everyAndOtherOverlap(new Every(currentExpression, new IntegerFieldValue(12)), otherExpression);
				} else {
					overlap = onAndOtherOverlap(currentExpression, otherExpression);
				}
			}
		} else if (And.class.equals(thisExpression.getClass())) {
			final FieldExpression finalOtherExpression = otherExpression;
			overlap = ((And) thisExpression).getExpressions().stream()
											.anyMatch(expression -> onAndOtherOverlap((On) expression, finalOtherExpression));
		}
		return overlap;
	}

	public boolean yearsOverlap(final Cron cron) {

		final FieldExpression thisYear = this.retrieve(CronFieldName.YEAR).getExpression();
		final FieldExpression otherYear = cron.retrieve(CronFieldName.YEAR).getExpression();
		final Object thisYearClass = thisYear.getClass();
		final Object otherYearClass = otherYear.getClass();
		boolean overlap = false;

		if (Always.class.equals(thisYearClass) || Always.class.equals(otherYearClass)) {
			overlap = true;
		} else if (On.class.equals(thisYear.getClass())) {
			overlap = onAndOtherOverlap((On) thisYear, otherYear);
		} else if (Every.class.equals(thisYear.getClass())) {
			overlap = everyAndOtherOverlap((Every) thisYear, otherYear);
		} else if (Between.class.equals(thisYear.getClass())) {
			final Integer startYear = (Integer) ((Between) thisYear).getFrom().getValue();
			final Integer endYear = (Integer) ((Between) thisYear).getTo().getValue();
			for (int i = startYear; i <= endYear && !overlap; i++) {
				final On currentYear = new On((new IntegerFieldValue(i)));
				overlap = onAndOtherOverlap(currentYear, otherYear);
			}
		} else if (And.class.equals(thisYear.getClass())) {
			overlap = ((And) thisYear).getExpressions().stream()
									  .anyMatch(year -> onAndOtherOverlap((On) year, otherYear));
		}
		return overlap;
	}

	private boolean onAndOtherOverlap(final On onYear, final FieldExpression otherYear) {

		final Integer onYearValue = onYear.getTime().getValue() != -1 ? onYear.getTime().getValue() : onYear.getNth().getValue();
		final Integer onYearPeriod = 0;
		Integer otherYearValue = null;
		Integer otherYearPeriod = null;
		if (On.class.equals(otherYear.getClass())) {
			otherYearValue = ((On) otherYear).getTime().getValue() != -1 ? ((On) otherYear).getTime().getValue() :
							 ((On) otherYear).getNth().getValue();
			otherYearPeriod = 0;
			return SequenceUtils.firstOverlap(onYearValue, onYearPeriod, otherYearValue, otherYearPeriod) < Integer.MAX_VALUE;
		} else if (Every.class.equals(otherYear.getClass())) {
			otherYearValue = ((On) ((Every) otherYear).getExpression()).getTime().getValue();
			otherYearPeriod = ((Every) otherYear).getPeriod().getValue();
			return SequenceUtils.firstOverlap(onYearValue, onYearPeriod, otherYearValue, otherYearPeriod) < Integer.MAX_VALUE;
		} else if (Between.class.equals(otherYear.getClass())) {
			final Integer startYear = (Integer) ((Between) otherYear).getFrom().getValue();
			final Integer endYear = (Integer) ((Between) otherYear).getTo().getValue();
			boolean overlap = false;
			for (int i = startYear; i <= endYear && !overlap; i++) {
				overlap = SequenceUtils.firstOverlap(onYearValue, onYearPeriod, i, 0) < Integer.MAX_VALUE;
			}
			return overlap;
		} else if (And.class.equals(otherYear.getClass())) {
			return ((And) otherYear).getExpressions().stream()
									.anyMatch(year -> SequenceUtils.firstOverlap(onYearValue, onYearPeriod,
																				 ((On) year).getTime().getValue(), 0) < Integer.MAX_VALUE);
		}
		return false;
	}

	private boolean everyAndOtherOverlap(final Every everyYearExpression, final FieldExpression otherYearExpression) {

		final Integer thisYearValue = ((On) everyYearExpression.getExpression()).getTime().getValue();
		final Integer thisYearPeriod = (everyYearExpression).getPeriod().getValue();
		if (On.class.equals(otherYearExpression.getClass())) {
			final Integer otherYearValue = ((On) otherYearExpression).getTime().getValue();
			final Integer otherYearPeriod = 0;
			return SequenceUtils.firstOverlap(thisYearValue, thisYearPeriod, otherYearValue, otherYearPeriod) < Integer.MAX_VALUE;
		} else if (Every.class.equals(otherYearExpression.getClass())) {
			final Integer otherYearValue = ((On) ((Every) otherYearExpression).getExpression()).getTime().getValue();
			final Integer otherYearPeriod = ((Every) otherYearExpression).getPeriod().getValue();
			return SequenceUtils.firstOverlap(thisYearValue, thisYearPeriod, otherYearValue, otherYearPeriod) < Integer.MAX_VALUE;
		} else if (Between.class.equals(otherYearExpression.getClass())) {
			final Integer startYear = (Integer) ((Between) otherYearExpression).getFrom().getValue();
			final Integer endYear = (Integer) ((Between) otherYearExpression).getTo().getValue();
			boolean overlap = false;
			for (int i = startYear; i <= endYear && !overlap; i++) {
				overlap = SequenceUtils.firstOverlap(thisYearValue, thisYearPeriod, i, 0) < Integer.MAX_VALUE;
			}
			return overlap;
		} else if (And.class.equals(otherYearExpression.getClass())) {
			return ((And) otherYearExpression).getExpressions().stream()
											  .anyMatch(year -> SequenceUtils.firstOverlap(thisYearValue, thisYearPeriod,
																						   ((On) year).getTime().getValue(), 0)
													  < Integer.MAX_VALUE);
		}
		return false;
	}

}
