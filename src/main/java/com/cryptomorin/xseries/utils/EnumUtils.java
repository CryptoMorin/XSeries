package com.cryptomorin.xseries.utils;

import static java.util.stream.Collectors.joining;

import java.util.Arrays;

public class EnumUtils 
{
	public static <E extends Enum<?>> String getDisplayName(E enumInstance) 
	{
		return Arrays.stream(enumInstance.name().split("_"))
                .map(word -> word.charAt(0) + word.substring(1).toLowerCase())
                .collect(joining(" "));
	}
}
