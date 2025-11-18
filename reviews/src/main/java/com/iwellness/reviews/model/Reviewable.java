package com.iwellness.reviews.model;

public interface Reviewable {
    Long getId();
    String getType();
    String getName();
    Long getProviderId(); // For services, returns the provider ID; for providers, returns their own ID
}