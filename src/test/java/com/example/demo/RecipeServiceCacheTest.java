package com.example.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.junit.jupiter.api.Assertions.assertThrows;
import com.example.demo.exception.RecipeNotFoundException;
import com.example.demo.exception.SpoonacularException;
import com.example.demo.exception.SpoonacularException.Reason;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.List;


@SpringBootTest
public class RecipeServiceCacheTest {

    @Autowired
    private RecipeService recipeService;

    @Autowired
    private CacheManager cacheManager;

    @MockitoBean
    private SpoonacularClient client;

    /** Antes de cada test vacía la caché y simula una respuesta correcta de Spoonacular. */
    @BeforeEach
    void setUp(){
        cacheManager.getCache("recipes").clear();
        when(client.searchRecipes(anyString())).thenReturn(new SpoonacularSearchResponse(List.of(
                new SpoonacularSearchResponse.Result(1L,"Pasta","https://img/1.jpg",45,4,List.of(),List.of()))));
    }

    /** Comprueba que repetir una búsqueda solo llama una vez a Spoonacular. */
    @Test
    void sameSearchCallsSpoonacularOnce(){
        recipeService.search("pasta");
        recipeService.search("pasta");
        verify(client,times(1)).searchRecipes("pasta");
    }

    /** Comprueba que dos búsquedas distintas llaman dos veces a Spoonacular. */
    @Test
    void differentSearchCallsSpoonacularAgain(){
        recipeService.search("pasta");
        recipeService.search("pizza");
        verify(client,times(2)).searchRecipes(anyString());
    }

    /** Comprueba que la caché no distingue mayúsculas ni espacios en los extremos. */
    @Test
    void sameSearchDifferentCaseOrSpacesCallsSpoonacularOnce(){
        recipeService.search("pasta");
        recipeService.search("Pasta");
        recipeService.search("pasta ");
        verify(client,times(1)).searchRecipes(anyString());
    }

    /** Comprueba que una búsqueda fallida no se guarda en caché. */
    @Test
    void failedSearchIsNotCached(){
        when(client.searchRecipes(anyString())).thenThrow( new SpoonacularException(Reason.UPSTREAM_ERROR,"fallo"));
        assertThrows(SpoonacularException.class,()->recipeService.search("pasta"));
        assertThrows(SpoonacularException.class,()->recipeService.search("pasta"));
        verify(client,times(2)).searchRecipes(anyString());
    }

    /** Comprueba que una búsqueda sin resultados no se guarda en caché. */
    @Test
    void searchWithoutResultsIsNotCached(){
        when(client.searchRecipes(anyString())).thenReturn(new SpoonacularSearchResponse(List.of()));
        assertThrows(RecipeNotFoundException.class,()->recipeService.search("xyz"));
        assertThrows(RecipeNotFoundException.class,()->recipeService.search("xyz"));
        verify(client,times(2)).searchRecipes(anyString());
    }
}
