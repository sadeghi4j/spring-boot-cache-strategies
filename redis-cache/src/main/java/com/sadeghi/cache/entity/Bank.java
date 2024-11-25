package com.sadeghi.cache.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * @author Ali Sadeghi
 * Created at 11/24/24 - 10:17 AM
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class Bank  {

    @Id
    Long id;

    String name;

    String code;

}
