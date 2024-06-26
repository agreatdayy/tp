package tutorpro.storage;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import tutorpro.commons.exceptions.IllegalValueException;
import tutorpro.model.person.Address;
import tutorpro.model.person.Email;
import tutorpro.model.person.Name;
import tutorpro.model.person.Person;
import tutorpro.model.person.Phone;
import tutorpro.model.schedule.Reminder;
import tutorpro.model.tag.Tag;

/**
 * Jackson-friendly version of {@link Reminder}.
 */
class JsonAdaptedReminder {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Reminder's %s field is missing!";

    protected final String name;
    protected final LocalDateTime time;
    protected final String notes;
    protected final List<String> people = new ArrayList<>();
    protected final List<JsonAdaptedTag> tags = new ArrayList<>();



    /**
     * Constructs a {@code JsonAdaptedReminder} with the given person details.
     */
    @JsonCreator
    public JsonAdaptedReminder(@JsonProperty("name") String name, @JsonProperty("time") LocalDateTime time,
                               @JsonProperty("notes") String notes,
                               @JsonProperty("people") List<String> people,
                               @JsonProperty("tags") List<JsonAdaptedTag> tags) {
        this.name = name;
        this.time = time;
        this.notes = notes;
        if (people != null) {
            this.people.addAll(people);
        }
        if (tags != null) {
            this.tags.addAll(tags);
        }
    }

    /**
     * Converts a given {@code Reminder} into this class for Jackson use.
     */
    public JsonAdaptedReminder(Reminder source) {
        name = source.getName();
        time = source.getTime();
        notes = source.getNotes();

        people.addAll(source.getPeople().stream()
                .map(person -> person.getName().toString())
                .collect(Collectors.toList()));

        tags.addAll(source.getTags().stream()
                .map(JsonAdaptedTag::new)
                .collect(Collectors.toList()));
    }

    /**
     * Converts this Jackson-friendly adapted reminder object into the model's {@code Reminder} object.
     *
     * @throws IllegalValueException if there were any data constraints violated in the adapted reminder.
     */
    public Reminder toModelType() throws IllegalValueException {
        final List<Person> reminderPeople = new ArrayList<>();
        for (String person : people) {
            reminderPeople.add(new Person(new Name(person), new Phone("88888888"),
                    new Email("a@bc"), new Address("0"), new HashSet<>()));
        }

        final List<Tag> reminderTags = new ArrayList<>();
        for (JsonAdaptedTag tag : tags) {
            reminderTags.add(tag.toModelType());
        }

        if (name == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Name.class.getSimpleName()));
        }
        if (!Name.isValidName(name)) {
            throw new IllegalValueException(Name.MESSAGE_CONSTRAINTS);
        }
        final String modelName = name;

        if (time == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT,
                    LocalDateTime.class.getSimpleName()));
        }
        final LocalDateTime modelTime = time;

        if (notes == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, String.class.getSimpleName()));
        }

        final String modelNotes = notes;

        final Set<Person> modelPeople = new HashSet<>(reminderPeople);

        final Set<Tag> modelTags = new HashSet<>(reminderTags);

        return new Reminder(modelName, modelTime, modelNotes, modelPeople, modelTags);
    }
}
