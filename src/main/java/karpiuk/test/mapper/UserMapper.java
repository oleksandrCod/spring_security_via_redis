package karpiuk.test.mapper;

import karpiuk.test.config.MapperConfiguration;
import karpiuk.test.dto.response.LoggedInUserResponse;
import karpiuk.test.model.User;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfiguration.class)
public interface UserMapper {

    LoggedInUserResponse toLoggedInResponseDto(User user);
}
