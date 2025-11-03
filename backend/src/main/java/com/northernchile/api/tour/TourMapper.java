package com.northernchile.api.tour;

import com.northernchile.api.model.Tour;
import com.northernchile.api.model.TourImage;
import com.northernchile.api.tour.dto.TourImageRes;
import com.northernchile.api.tour.dto.TourRes;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TourMapper {

    TourRes toTourRes(Tour tour);

    List<TourRes> toTourResList(List<Tour> tours);
    
    TourImageRes toTourImageRes(TourImage tourImage);
}
