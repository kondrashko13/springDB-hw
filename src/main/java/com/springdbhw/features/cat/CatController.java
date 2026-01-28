package com.springdbhw.features.cat;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cats")
public class CatController {

    private final CatService catService;

    public CatController(CatService catService) {
        this.catService = catService;
    }

    @GetMapping
    public List<Cat> getAllCats() {
        return catService.getAllCats();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cat> getCatById(@PathVariable Long id) {
        Cat cat = catService.getCatById(id);
        if (cat == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(cat);
    }

    @PostMapping
    public ResponseEntity<String> createCat(@RequestBody Cat cat) {
        catService.createCat(cat);
        return ResponseEntity.ok("Cat created!");
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateCat(@PathVariable Long id, @RequestBody Cat cat) {
        cat.setId(id);
        catService.updateCat(cat);
        return ResponseEntity.ok("Cat updated!");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCat(@PathVariable Long id) {
        catService.deleteCat(id);
        return ResponseEntity.ok("Cat deleted!");
    }
}
