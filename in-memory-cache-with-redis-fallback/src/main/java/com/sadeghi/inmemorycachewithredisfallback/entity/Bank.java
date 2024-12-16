package com.sadeghi.inmemorycachewithredisfallback.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

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
public class Bank implements Serializable {

    @Id
    Long id;

    String name;

    String code;

}
