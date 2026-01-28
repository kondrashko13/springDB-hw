package com.springdbhw.features.cat;

import com.springdbhw.features.cat.Cat;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CatRepository {

    private final JdbcTemplate jdbcTemplate;

    public CatRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Cat> catRowMapper = (rs, rowNum) ->
            new Cat(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getInt("age")
            );

    public List<Cat> findAll() {
        return jdbcTemplate.query("SELECT * FROM cats", catRowMapper);
    }

    public Cat findById(Long id) {
        return jdbcTemplate.queryForObject(
                "SELECT * FROM cats WHERE id = ?",
                catRowMapper,
                id
        );
    }

    public int save(Cat cat) {
        return jdbcTemplate.update(
                "INSERT INTO cats (name, age) VALUES (?, ?)",
                cat.getName(), cat.getAge()
        );
    }

    public int update(Cat cat) {
        return jdbcTemplate.update(
                "UPDATE cats SET name = ?, age = ? WHERE id = ?",
                cat.getName(), cat.getAge(), cat.getId()
        );
    }

    public int delete(Long id) {
        return jdbcTemplate.update("DELETE FROM cats WHERE id = ?", id);
    }
}
