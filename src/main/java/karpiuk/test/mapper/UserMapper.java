package karpiuk.test.mapper;

import karpiuk.test.config.MapperConfiguration;
import karpiuk.test.dto.LoggedInUserInformationResponseDto;
import karpiuk.test.model.User;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfiguration.class)
public interface UserMapper {

    LoggedInUserInformationResponseDto toLoggedInResponseDto(User user);
}
