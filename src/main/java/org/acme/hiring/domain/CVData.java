package org.acme.hiring.domain;

import java.util.List;

public record CVData(String candidateName, List<String> skills, int yearsExperience) {

}
