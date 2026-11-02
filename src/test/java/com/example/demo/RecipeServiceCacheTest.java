package com.example.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.junit.jupiter.api.Assertions.assertThrows;
import com.example.demo.exception.SpoonacularException;
import com.example.demo.exception.SpoonacularException.Reason;
import static org.mockito.ArgumentMatchers.anyInt;
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

    @BeforeEach
    void setUp(){
        cacheManager.getCache("recipes").clear();
        when(client.searchRecipes(anyString(),anyInt(),anyInt())).thenReturn(new SpoonacularSearchResponse(List.of(),0,10,0));
    }

    @Test
    void sameSearchCallsSpoonacularOnce(){
        recipeService.search("pasta",10,0);
        recipeService.search("pasta",10,0);
        verify(client,times(1)).searchRecipes("pasta",10,0);
    }

    @Test
    void differentOffsetCallsSpoonacularAgain(){
        recipeService.search("pasta",10,0);
        recipeService.search("pasta",10,10);
        verify(client,times(2)).searchRecipes(anyString(),anyInt(),anyInt());
    }

    @Test
    void sameSearchDifferentCaseOrSpacesCallsSpoonacularOnce(){
        recipeService.search("pasta",10,0);
        recipeService.search("Pasta",10,0);
        recipeService.search("pasta ",10,0);
        verify(client,times(1)).searchRecipes(anyString(),anyInt(),anyInt());
    }

    @Test
    void failedSearchIsNotCached(){
        when(client.searchRecipes(anyString(),anyInt(),anyInt())).thenThrow( new SpoonacularException(Reason.UPSTREAM_ERROR,"fallo"));
        assertThrows(SpoonacularException.class,()->recipeService.search("pasta",10,0));
        assertThrows(SpoonacularException.class,()->recipeService.search("pasta",10,0));
        verify(client,times(2)).searchRecipes(anyString(),anyInt(),anyInt());
    }
}
