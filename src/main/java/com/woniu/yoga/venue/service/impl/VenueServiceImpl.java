package com.woniu.yoga.venue.service.impl;

import com.woniu.yoga.user.pojo.Coach;
import com.woniu.yoga.user.pojo.Course;
import com.woniu.yoga.user.pojo.User;
import com.woniu.yoga.venue.dao.VenueMapper;
import com.woniu.yoga.venue.pojo.Recruitment;
import com.woniu.yoga.venue.pojo.Venue;
import com.woniu.yoga.venue.repository.VenueRepository;
import com.woniu.yoga.venue.service.VenueService;
import com.woniu.yoga.venue.vo.CoachInformationVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
@Service
@Repository
public class VenueServiceImpl implements VenueService{
    @Autowired
    private VenueMapper venueMapper;
    @Autowired
    private VenueRepository venueRepository;

    @Override
    public void saveVenue(Venue venue) {
        venueRepository.save(venue);
    }

    @Override
    public List<Coach> findCoachByVagueConditions(Coach coach, BigDecimal up_expected_salary, BigDecimal down_expected_salary) {
        List<Coach> listcoach = new ArrayList<>();
        venueMapper.queryCoachByVagueConditions(coach,up_expected_salary,down_expected_salary);
        return listcoach;
    }

    @Override
    public Venue findVenueByVenueId(Integer venueId) {
        return venueMapper.selectByPrimaryKey(venueId);
    }

    @Override
    public int waitCoachForSign(Integer venueId, Integer coachId) {
        return venueMapper.waitForSign(venueId,coachId);
    }

    @Override
    public int coachSignService(Integer cv_id) {
        return venueMapper.coachSignMapper(cv_id);
    }

    @Override
    public int coachRefuseService(int cv_id) {
        return venueMapper.coachRefuseMapper(cv_id);
    }

    @Override
    public int venueAddRecruitService(Recruitment recruitment) {
        return venueMapper.venueAddRecruitMapper(recruitment);
    }

    @Override
    public List<CoachInformationVO> venueFindCoach(CoachInformationVO coachInformationVO) {
        List list = venueMapper.venueQueryCoach(coachInformationVO);
        return list;
    }

    @Override
    public int venueBreakCoachService(int coachId) {

        return venueMapper.venueBreakCoachMapper(coachId);
    }

    @Override
    public int venuePerfectInformationService(Venue venue) {
        return venueMapper.venuePerfectInformationMapper(venue);
    }

    @Override
    public int addVenueUserInformationService(Integer userId, User user) {
        return venueMapper.addVenueUserInformationMapper(userId,user);
    }

    @Override
    public List<Coach> findCoachByVenueIdService(Integer venueId) {
        return venueMapper.queryCoachByVenueId(venueId);
    }

    @Override
    public int coachAddCourseService(Course course) {
        return venueMapper.coachAddCourseMapper(course);
    }


}
