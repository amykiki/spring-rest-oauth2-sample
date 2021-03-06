package com.saintdan.framework.controller;

import com.saintdan.framework.annotation.CurrentUser;
import com.saintdan.framework.component.ResultHelper;
import com.saintdan.framework.component.ValidateHelper;
import com.saintdan.framework.constant.ControllerConstant;
import com.saintdan.framework.constant.ResourceURL;
import com.saintdan.framework.constant.ResultConstant;
import com.saintdan.framework.constant.VersionConstant;
import com.saintdan.framework.domain.UserDomain;
import com.saintdan.framework.enums.ErrorType;
import com.saintdan.framework.enums.OperationStatus;
import com.saintdan.framework.enums.OperationType;
import com.saintdan.framework.exception.CommonsException;
import com.saintdan.framework.param.UserParam;
import com.saintdan.framework.po.User;
import com.saintdan.framework.tools.QueryHelper;
import com.saintdan.framework.vo.ResultVO;
import com.saintdan.framework.vo.UserVO;
import javax.validation.Valid;
import net.kaczmarzyk.spring.data.jpa.domain.DateBetween;
import net.kaczmarzyk.spring.data.jpa.domain.In;
import net.kaczmarzyk.spring.data.jpa.domain.Like;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller of user.
 *
 * @author <a href="http://github.com/saintdan">Liao Yifan</a>
 * @date 6/25/15
 * @since JDK1.8
 */
@RestController
@RequestMapping(ResourceURL.RESOURCES + VersionConstant.V1 + ResourceURL.USERS)
public class UserController {

  // ------------------------
  // PUBLIC METHODS
  // ------------------------

  /**
   * Create new {@link User}.
   *
   * @param param {@link UserParam}
   * @return {@link com.saintdan.framework.vo.UserVO}
   */
  @RequestMapping(method = RequestMethod.POST)
  public ResultVO create(@CurrentUser User currentUser, @Valid UserParam param, BindingResult result) {
    try {
      // Validate current user, param and sign.
      ResultVO resultVO = validateHelper.validate(param, result, currentUser, logger, OperationType.CREATE);
      if (resultVO != null) {
        return resultVO;
      }
      // Return result and message.
      return resultHelper.successResp(userDomain.create(param, currentUser));
    } catch (CommonsException e) {
      // Return error information and log the exception.
      return resultHelper.infoResp(logger, e.getErrorType(), e.getMessage());
    } catch (Exception e) {
      // Return unknown error and log the exception.
      return resultHelper.errorResp(logger, e, ErrorType.UNKNOWN, e.getMessage());
    }
  }

  /**
   * Show all.
   *
   * @param param {@link UserParam}
   * @return users
   */
  @RequestMapping(method = RequestMethod.GET)
  public ResultVO all(
      @And({
          @Spec(path = "usr", spec = Like.class),
          @Spec(path = "name", spec = Like.class),
          @Spec(path = "validFlag", constVal = "VALID", spec = In.class),
          @Spec(path = "createdDate", params = {"createdDateAfter", "createdDateBefore"}, spec = DateBetween.class)}) Specification<User> userSpecification,
      UserParam param
  ) {
    try {
      if (param.getPageNo() == null) {
        return resultHelper.successResp(userDomain.getAll(userSpecification, QueryHelper.getSort(param.getSortBy()), UserVO.class));
      }
      return resultHelper.successResp(userDomain.getPage(userSpecification, QueryHelper.getPageRequest(param), UserVO.class));
    } catch (CommonsException e) {
      // Return error information and log the exception.
      return resultHelper.infoResp(logger, e.getErrorType(), e.getMessage());
    } catch (Exception e) {
      // Return unknown error and log the exception.
      return resultHelper.errorResp(logger, e, ErrorType.UNKNOWN, e.getMessage());
    }
  }

  /**
   * Show {@link com.saintdan.framework.vo.UserVO} by ID.
   *
   * @param id {@link User#id}
   * @return {@link com.saintdan.framework.vo.UserVO}
   */
  @RequestMapping(value = "/{id}", method = RequestMethod.GET)
  public ResultVO detail(@PathVariable String id) {
    try {
      if (StringUtils.isBlank(id)) {
        return resultHelper.infoResp(ErrorType.SYS0002, String.format(ControllerConstant.PARAM_BLANK, ControllerConstant.ID_PARAM));
      }
      return resultHelper.successResp(userDomain.getById(Long.valueOf(id), UserVO.class));
    } catch (CommonsException e) {
      // Return error information and log the exception.
      return resultHelper.infoResp(logger, e.getErrorType(), e.getMessage());
    } catch (Exception e) {
      // Return unknown error and log the exception.
      return resultHelper.errorResp(logger, e, ErrorType.UNKNOWN, e.getMessage());
    }
  }

  /**
   * Update {@link User}.
   *
   * @param id    {@link User#id}
   * @param param {@link UserParam}
   * @return {@link com.saintdan.framework.vo.UserVO}
   */
  @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
  public ResultVO update(@CurrentUser User currentUser, @PathVariable String id, @Valid UserParam param, BindingResult result) {
    try {
      param.setId(StringUtils.isBlank(id) ? null : Long.valueOf(id));
      // Validate current user, param and sign.
      ResultVO resultVO = validateHelper.validate(param, result, currentUser, logger, OperationType.UPDATE);
      if (resultVO != null) {
        return resultVO;
      }
      // Update user.
      return resultHelper.successResp(userDomain.update(param, currentUser));
    } catch (CommonsException e) {
      // Return error information and log the exception.
      return resultHelper.infoResp(logger, e.getErrorType(), e.getMessage());
    } catch (Exception e) {
      // Return unknown error and log the exception.
      return resultHelper.errorResp(logger, e, ErrorType.UNKNOWN, e.getMessage());
    }
  }

  /**
   * Delete {@link User}.
   *
   * @param id {@link User#id}
   * @return {@link com.saintdan.framework.vo.UserVO}
   */
  @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
  public ResultVO delete(@CurrentUser User currentUser, @PathVariable String id) {
    try {
      UserParam param = new UserParam(StringUtils.isBlank(id) ? null : Long.valueOf(id));
      // Validate current user and param.
      ResultVO resultVO = validateHelper.validate(param, currentUser, logger, OperationType.DELETE);
      if (resultVO != null) {
        return resultVO;
      }
      // Delete user.
      userDomain.delete(param, currentUser);
      final String USER = "user";
      return new ResultVO(ResultConstant.OK, OperationStatus.SUCCESS, String.format(ControllerConstant.DELETE, USER));
    } catch (CommonsException e) {
      // Return error information and log the exception.
      return resultHelper.infoResp(logger, e.getErrorType(), e.getMessage());
    } catch (Exception e) {
      // Return unknown error and log the exception.
      return resultHelper.errorResp(logger, e, ErrorType.UNKNOWN, e.getMessage());
    }
  }

  // ------------------------
  // PRIVATE FIELDS
  // ------------------------

  private static final Logger logger = LoggerFactory.getLogger(UserController.class);

  @Autowired private ResultHelper resultHelper;

  @Autowired private ValidateHelper validateHelper;

  @Autowired private UserDomain userDomain;

}
