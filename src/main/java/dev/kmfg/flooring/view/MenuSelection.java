package dev.kmfg.flooring.view;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum MenuSelection {
    NONE(0),
    EXIT(1),
    DISPLAY_ORDERS(2),
    ADD_ORDER(3),
    EDIT_ORDER(4),
    REMOVE_ORDER(5),
    EXPORT_ALL_ORDERS(6)
    ;

    private final int value;

    MenuSelection(final int value) {
        this.value = value;
    }

    public String getNiceName() {
        return Arrays.stream(this.name().split("_"))
                .map(this::capitalizeWord)
                .collect(Collectors.joining(" "));
    }

    private String capitalizeWord(String word) {
        if(word.isEmpty()) {
            return "";
        }

        final char firstChar = Character.toUpperCase(word.charAt(0));
        final String restOfWord = word.length() > 1 ? word.substring(1).toLowerCase() : "";

        return firstChar + restOfWord;
    }


    public static MenuSelection fromInt(int i) {
        final MenuSelection[] values = values();
        for(int j = 1; j < values.length; j++) {
            if(values[j].value == i) {
                return values[j];
            }
        }
        return MenuSelection.NONE;
    }
}
