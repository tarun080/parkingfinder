package com.example.parkingfinder.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.parkingfinder.database.entities.UserEntity;

@Dao
public interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UserEntity user);

    @Update
    void update(UserEntity user);

    @Delete
    void delete(UserEntity user);

    @Query("SELECT * FROM users WHERE uid = :uid")
    LiveData<UserEntity> getUserById(String uid);

    @Query("SELECT * FROM users WHERE uid = :uid")
    UserEntity getUserByIdSync(String uid);

    @Query("DELETE FROM users")
    void deleteAll();
}