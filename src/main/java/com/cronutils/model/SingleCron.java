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
import com.cronutils.utils.Preconditions;

import java.util.*;

public class SingleCron implements Cron {
    private static final long serialVersionUID = 7487370826825439098L;
    private final CronDefinition cronDefinition;
    private final Map<CronFieldName, CronField> fields;
    private String asString;

    /**
     * Creates a Cron with the iven cron definition and the given fields.
     * @param cronDefinition the definition to use for this Cron
     * @param fields the fields that should be used
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
     * @param name - cron field name.
     *             If null, a NullPointerException will be raised.
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
                throw new IllegalArgumentException(String.format("Invalid cron expression: %s. %s", asString(), constraint.getDescription()));
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
     * Provides means to compare if two cron expressions are equivalent.
     * Assumes same cron definition.
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

	public boolean yearsOverlap(final Cron cron) {

		final FieldExpression thisYear = this.retrieve(CronFieldName.YEAR).getExpression();
		final FieldExpression otherYear = cron.retrieve(CronFieldName.YEAR).getExpression();
		boolean overlap = false;

		if (Always.class.equals(thisYear.getClass()) || Always.class.equals(otherYear.getClass())) {
			overlap = true;
		}
		else if (On.class.equals(thisYear.getClass())) {
			overlap = onAndOtherOverlap((On) thisYear, otherYear);
		}
		else if(Every.class.equals(thisYear.getClass())) {
			if(On.class.equals(otherYear.getClass())) {
				overlap = yearsOnAndEveryOverlap((On) otherYear, (Every) thisYear);
			}
		}
		else if(Between.class.equals(thisYear.getClass())) {
			if(On.class.equals(otherYear.getClass())) {
				overlap = yearsOnAndBetweenOverlap((On) otherYear, (Between) thisYear);
			}
		}
		else if(And.class.equals(thisYear.getClass())) {
			overlap = yearsOnAndOverlap((On) otherYear, (And) thisYear);
		}
		return overlap;
	}

	private boolean onAndOtherOverlap(final On thisYear, final FieldExpression otherYear) {
		boolean overlap = false;
		if (On.class.equals(otherYear.getClass())) {
			overlap = thisYear.getTime().getValue().equals(((On) otherYear).getTime().getValue());
		}
		else if (Every.class.equals(otherYear.getClass())) {
			overlap = yearsOnAndEveryOverlap(thisYear, (Every) otherYear);
		}
		else if (Between.class.equals(otherYear.getClass())) {
			overlap = yearsOnAndBetweenOverlap(thisYear, (Between) otherYear);
		}
		else if (And.class.equals(otherYear.getClass())) {
			overlap = yearsOnAndOverlap(thisYear, (And) otherYear);
		}
		return overlap;
	}

	public boolean yearsOnAndOverlap(final On onYearExpression, final And andYearExpression) {
		return andYearExpression.getExpressions().stream()
								.anyMatch(year -> ((On) year).getTime().getValue().equals((onYearExpression).getTime().getValue()));
	}

	public boolean yearsOnAndBetweenOverlap(final On onYearExpression, final Between betweenYearsExpression) {

		return (onYearExpression).getTime().getValue() >= (Integer) (betweenYearsExpression).getFrom().getValue()
				|| (onYearExpression).getTime().getValue() <= (Integer) (betweenYearsExpression).getTo().getValue();
	}

	public boolean yearsOnAndEveryOverlap(final On onYearExpression, final Every everyYearsExpression) {

		// If there is an overlap where a specific year is included in a year sequence, the number of years until an overlap happens
		// will be a positive integer in the following equation:
		// numberOfYearsUntilOverlap = (specificYear - startYear)/period
		final Double startYear = ((On) everyYearsExpression.getExpression()).getTime().getValue().doubleValue();
		final Double period = everyYearsExpression.getPeriod().getValue().doubleValue();
		final Double yearsForOverlap = ( onYearExpression.getTime().getValue() - startYear) / period;

		return (yearsForOverlap > 0 && yearsForOverlap % 1 == 0);
	}
}
